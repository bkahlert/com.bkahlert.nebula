package com.bkahlert.nebula.screenshots;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Instances of this class provide the functionality to take screenshots.
 * 
 * @author bkahlert
 * 
 * @param <SUBJECT>
 *            the type of objects that this {@link IScreenshotTaker} can render
 */
public interface IScreenshotTaker<SUBJECT> {

	public enum Format {
		PNG, JPEG, GIF;

		public String getName() {
			switch (this) {
			case JPEG:
				return "jpg";
			}
			return this.toString().toLowerCase();
		}
	}

	/**
	 * Submit the given subject and immediately returns a {@link Future}
	 * containing the location of the generated screenshot.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param subject
	 * @param format
	 *            TODO
	 * 
	 * @return
	 */
	public Future<File> takeScreenshot(SUBJECT subject, Format format);

	/**
	 * Submit the given subjects and immediately returns a {@link List} of
	 * {@link Future} containing the locations of the generated screenshots.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param subjects
	 * @param format
	 *            TODO
	 * @return
	 */
	public List<Future<File>> takeScreenshots(List<SUBJECT> subjects,
			Format format);

	/**
	 * Disposes all resources this {@link IScreenshotTaker} occupies.
	 */
	public void dispose();

}
