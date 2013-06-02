package com.bkahlert.nebula.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;

public class ShellUtils {

	public static Robot robot;

	public static void setVisible(final Shell shell, final boolean visible) {
		try {
			ExecutorUtil.syncExec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					shell.setVisible(visible);
					return null;
				}

			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void bringToFront(final Shell shell) {
		try {
			ExecutorUtil.syncExec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					shell.setActive();
					return null;
				}

			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the {@link Dialog}'s client area relative to the screen.
	 * <p>
	 * This is the coordinates of the window without its trim.
	 * 
	 * @return
	 */
	public static Rectangle getInnerArea(final Shell shell) {
		try {
			return ExecutorUtil.syncExec(new Callable<Rectangle>() {
				@Override
				public Rectangle call() throws Exception {
					// window bounds
					org.eclipse.swt.graphics.Rectangle area = shell.getBounds();

					// window bounds + trim (= 2 borders)
					org.eclipse.swt.graphics.Rectangle trimArea = shell
							.computeTrim(area.x, area.y, area.width,
									area.height);

					// subtract what SWT wanted to add to remove the border
					area.x -= (trimArea.x - area.x);
					area.y -= (trimArea.y - area.y);
					area.width -= (trimArea.width - area.width);
					area.height -= (trimArea.height - area.height);
					return area;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Captures the given shell without its trim.
	 * 
	 * @param dialog
	 * @return
	 * 
	 * @ArbitraryThread
	 */
	public static BufferedImage captureScreen(Shell shell) {
		return captureScreen(getInnerArea(shell));
	}

	/**
	 * Captures the screen at the given coordinates.
	 * 
	 * @param dialog
	 * @return
	 * 
	 * @ArbitraryThread
	 */
	public static BufferedImage captureScreen(Rectangle area) {
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new RuntimeException(e);
			}
		}

		BufferedImage bufferedImage = robot
				.createScreenCapture(new java.awt.Rectangle(area.x, area.y,
						area.width, area.height));

		return bufferedImage;
	}

}
