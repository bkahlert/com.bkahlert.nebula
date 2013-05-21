package com.bkahlert.devel.nebula.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class FileUtils {
	public static void showFileInFilesystem(File file)
			throws InterruptedException, IOException {
		Assert.isNotNull(file);
		String osName = System.getProperty("os.name");
		if (osName.contains("Mac OS X")) {
			List<String> args = new ArrayList<String>();
			args.add("osascript");
			args.add("-e");
			args.add("tell application \"Finder\"");
			args.add("-e");
			args.add("activate");
			args.add("-e");
			args.add("reveal POSIX file \"" + file.getAbsolutePath() + "\"");
			args.add("-e");
			args.add("end tell");
			Runtime.getRuntime().exec(args.toArray(new String[0])).waitFor();
		} else {
			throw new UnsupportedOperationException(
					"Opening files in your OS is not supported.");
		}
	}

	public static File getTempDirectory() {
		File systemTempDirectory = new File(
				System.getProperty("java.io.tmpdir"));
		File tempDirectory = new File(systemTempDirectory, new BigInteger(130,
				new SecureRandom()).toString(32));
		tempDirectory.deleteOnExit();
		tempDirectory.mkdir();
		return tempDirectory;
	}

	public static File getTempFile(String prefix, String suffix) {
		try {
			File tempFile = File.createTempFile(prefix, suffix);
			tempFile.deleteOnExit();
			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File getTempFile() {
		return getTempFile(
				new BigInteger(130, new SecureRandom()).toString(32), ".tmp");
	}
}
