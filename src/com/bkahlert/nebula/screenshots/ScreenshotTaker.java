package com.bkahlert.nebula.screenshots;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.JQueryEnabledBrowserComposite;
import com.bkahlert.nebula.dialogs.BrowserDialog;
import com.bkahlert.nebula.dialogs.BrowserException;

public class ScreenshotTaker {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ScreenshotTaker.class);
	public static final String SCREENSHOT_FILE_PREFIX = "screenshot";

	public interface ScreenshotProcessor {
		public void beforeScreenshot(
				JQueryEnabledBrowserComposite browserComposite);
	}

	private Robot robot;
	private BrowserDialog dialog;
	private String lastUrl;

	public ScreenshotTaker() throws PartInitException, AWTException {
		this.robot = new Robot();
		this.dialog = new BrowserDialog(null);
		this.lastUrl = null;
	}

	/**
	 * Determines the maximum takeable screenshot size of this computer.
	 * 
	 * @UI
	 * @return
	 */
	public org.eclipse.swt.graphics.Rectangle getMaxCaptureArea() {
		BrowserDialog dialog = new BrowserDialog(null);
		dialog.setBlockOnOpen(false);
		dialog.open();
		dialog.getShell().setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		org.eclipse.swt.graphics.Rectangle maxCaptureArea = this
				.getCaptureArea(dialog);
		dialog.close();
		return maxCaptureArea;
	}

	/**
	 * Creates and returns a screenshot for the given {@link ScreenshotInfo}.
	 * <p>
	 * Note: The temporary file gets deleted when the virtual machine
	 * terminates. In order to persist the file you have to copy it.
	 * 
	 * @param screenshot
	 * @param format
	 * @return
	 * @throws BrowserException
	 * @blocking
	 */
	public Future<File> takeScreenshot(final ScreenshotInfo screenshot,
			final String format, final int timeout,
			final ScreenshotProcessor screenshotProcessor)
			throws BrowserException {
		if (Display.getCurrent() != null) {
			throw new SWTException(
					"This method is long running and must *not* be opened in the UI Thread.");
		}

		String url = screenshot.getUrl() != this.lastUrl ? screenshot.getUrl()
				: null;
		return ScreenshotTaker.this.dialog.open(url,
				screenshot.getWindowDimensions(),
				screenshot.getScrollPosition(), timeout, new Callable<File>() {
					@Override
					public File call() throws Exception {
						if (screenshotProcessor != null) {
							screenshotProcessor
									.beforeScreenshot(ScreenshotTaker.this.dialog
											.getBrowser());
						}

						return ExecutorUtil.syncExec(new Callable<File>() {
							@Override
							public File call() throws Exception {
								BufferedImage image = ScreenshotTaker.this
										.captureScreen(ScreenshotTaker.this.dialog);
								ScreenshotTaker.this.lastUrl = screenshot
										.getUrl();
								return ScreenshotTaker.this.imageToTempFile(
										image, format);
							}
						});
					}
				});
	}

	/**
	 * Calculates the {@link Dialog}'s client area relative to the screen.
	 * 
	 * @return
	 */
	protected org.eclipse.swt.graphics.Rectangle getCaptureArea(Dialog dialog) {
		// window bounds
		org.eclipse.swt.graphics.Rectangle area = dialog.getShell().getBounds();

		// window bounds + trim (= 2 borders)
		org.eclipse.swt.graphics.Rectangle trimArea = dialog.getShell()
				.computeTrim(area.x, area.y, area.width, area.height);

		// subtract what SWT wanted to add to remove the border
		area.x -= (trimArea.x - area.x);
		area.y -= (trimArea.y - area.y);
		area.width -= (trimArea.width - area.width);
		area.height -= (trimArea.height - area.height);
		return area;
	}

	/**
	 * Makes a screenshot of the given coordinates.
	 * 
	 * @param dialog
	 * @return
	 */
	protected BufferedImage captureScreen(Dialog dialog) {
		org.eclipse.swt.graphics.Rectangle area = this.getCaptureArea(dialog);

		BufferedImage bufferedImage = this.robot
				.createScreenCapture(new Rectangle(area.x, area.y, area.width,
						area.height));

		return bufferedImage;
	}

	/**
	 * Writes the given image to a temp file and returns it.
	 * <p>
	 * Note: The temporary file gets deleted when the virtual machine
	 * terminates.
	 * 
	 * @param bufferedImage
	 * @param format
	 * @return
	 * @throws IOException
	 */
	protected File imageToTempFile(BufferedImage bufferedImage, String format)
			throws IOException {
		File tempFile = File.createTempFile(SCREENSHOT_FILE_PREFIX, "."
				+ format);
		tempFile.deleteOnExit();
		ImageIO.write(bufferedImage, format, tempFile);
		return tempFile;
	}

	public void close() {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				ScreenshotTaker.this.dialog.close();
			}
		});
	}

}
