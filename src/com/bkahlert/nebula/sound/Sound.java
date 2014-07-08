package com.bkahlert.nebula.sound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;

import com.bkahlert.nebula.utils.ExecUtils;

public class Sound {

	private static final Logger LOGGER = Logger.getLogger(Sound.class);

	public static class SoundException extends Exception {
		private static final long serialVersionUID = 1L;

		public SoundException(Throwable cause) {
			super(cause);
		}
	}

	private static final int BUFFER_SIZE = 128000;

	private static void play(AudioInputStream audioInputStream)
			throws SoundException {
		try {
			AudioFormat audioFormat = audioInputStream.getFormat();

			Info info = new Info(SourceDataLine.class, audioFormat);
			SourceDataLine sourceLine = (SourceDataLine) AudioSystem
					.getLine(info);
			sourceLine.open(audioFormat);

			sourceLine.start();

			int nBytesRead = 0;
			byte[] abData = new byte[BUFFER_SIZE];
			while (nBytesRead != -1) {
				try {
					nBytesRead = audioInputStream
							.read(abData, 0, abData.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (nBytesRead >= 0) {
					sourceLine.write(abData, 0, nBytesRead);
				}
			}

			sourceLine.drain();
			sourceLine.close();
			audioInputStream.close();
		} catch (Exception e) {
			throw new SoundException(e);
		}
	}

	private final File file;
	private final InputStream inputStream;
	private final URL url;

	public Sound(File file) {
		this.file = file;
		this.inputStream = null;
		this.url = null;
	}

	public Sound(InputStream inputStream) {
		this.file = null;
		this.inputStream = inputStream;
		this.url = null;
	}

	public Sound(URL url) {
		this.file = null;
		this.inputStream = null;
		this.url = url;
	}

	private Callable<Void> getCallable() {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					AudioInputStream audioInputStream = null;
					if (Sound.this.file != null) {
						audioInputStream = AudioSystem
								.getAudioInputStream(Sound.this.file);
					} else if (Sound.this.inputStream != null) {
						audioInputStream = AudioSystem
								.getAudioInputStream(Sound.this.inputStream);
					} else {
						audioInputStream = AudioSystem
								.getAudioInputStream(Sound.this.url);
					}
					play(audioInputStream);
					return null;
				} catch (SoundException e) {
					throw e;
				} catch (Exception e) {
					throw new SoundException(e);
				}
			}
		};
	}

	public void play() throws SoundException {
		try {
			this.getCallable().call();
		} catch (SoundException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.fatal("This should never occur");
		}
	}

	public Future<Void> playAsync() {
		return ExecUtils.nonUISyncExec(this.getCallable());
	}

	public static void main(String[] args) throws SoundException,
			InterruptedException {
		Sound sound = new Sound(new File(
				"/Users/bkahlert/Downloads/Startup_WAVs/Startup1.wav"));
		sound.play();
		sound.playAsync();
		// System.err.println("played once");
		// Thread.sleep(1000);
		// sound.play();
		// System.err.println("played twice");
		//
		// Sound sound2 = new Sound(new File(
		// "/Users/bkahlert/Downloads/Startup_WAVs/Startup1.wav"));
		// sound2.play();
		// System.err.println("played once");
	}

}