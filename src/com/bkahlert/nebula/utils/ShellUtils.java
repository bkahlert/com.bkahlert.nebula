package com.bkahlert.nebula.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.ExecUtils.ParametrizedCallable;

public class ShellUtils {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ShellUtils.class);

	private static Robot robot;

	public static Robot getRobot() {
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				throw new RuntimeException(e);
			}
		}
		return robot;
	}

	public static void setVisible(final Shell shell, final boolean visible) {
		try {
			ExecUtils.syncExec(new Callable<Void>() {
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
			ExecUtils.syncExec(new Callable<Void>() {
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
	 * Calculates the {@link Shell}'s trim.
	 * 
	 * @return
	 */
	public static Rectangle getTrim(final Shell shell) {
		try {
			return ExecUtils.syncExec(new Callable<Rectangle>() {
				@Override
				public Rectangle call() throws Exception {
					// window bounds
					org.eclipse.swt.graphics.Rectangle area = shell.getBounds();

					// window bounds + trim (= 2 borders)
					org.eclipse.swt.graphics.Rectangle trim = shell
							.computeTrim(area.x, area.y, area.width,
									area.height);

					return trim;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the client area of the {@link Shell} belong to the given
	 * {@link Control} relative to the screen.
	 * <p>
	 * This is the coordinates of the window without its trim.
	 * 
	 * @return
	 */
	public static Rectangle getInnerArea(final Control control) {
		try {
			return ExecUtils.syncExec(new Callable<Rectangle>() {
				@Override
				public Rectangle call() throws Exception {
					return getInnerArea(control.getShell());
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the {@link Shell}'s client area relative to the screen.
	 * <p>
	 * This is the coordinates of the window without its trim.
	 * 
	 * @return
	 */
	public static Rectangle getInnerArea(final Shell shell) {
		try {
			return ExecUtils.syncExec(new Callable<Rectangle>() {
				@Override
				public Rectangle call() throws Exception {
					// window bounds
					org.eclipse.swt.graphics.Rectangle area = shell.getBounds();

					// window bounds + trim (= 2 borders)
					org.eclipse.swt.graphics.Rectangle trimArea = getTrim(shell);

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
	 * Captures the given {@link Control} without its trim.
	 * <p>
	 * <strong>Important:</strong> Remember to dispose the newly created
	 * {@link Image}.
	 * 
	 * @param dialog
	 * @return
	 * @throws Exception
	 * 
	 * @ArbitraryThread
	 */
	public static Image captureScreen(final Control control) throws Exception {
		return ExecUtils.syncExec(new Callable<Image>() {
			@Override
			public Image call() throws Exception {
				Point size = control.getSize();
				GC gc = new GC(control);
				Image image = new Image(Display.getCurrent(), size.x, size.y);
				gc.copyArea(image, 0, 0);
				gc.dispose();
				return image;
			}
		});
	}

	/**
	 * Captures the given shell without its trim.
	 * <p>
	 * <strong>Important:</strong> The caller has to make sure the {@link Shell}
	 * in question is actually visible and not overlapped.
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
		Robot robot = getRobot();

		BufferedImage bufferedImage = robot
				.createScreenCapture(new java.awt.Rectangle(area.x, area.y,
						area.width, area.height));

		return bufferedImage;
	}

	public static <T> void runInSeparateShell(int width, int height,
			final ParametrizedCallable<Shell, Future<T>> runner, int timeout)
			throws Exception {
		Display display = new Display();
		final Shell shell = new Shell(display);

		shell.setLayout(new FillLayout());

		final Future<T> rt = runner.call(shell);
		final AtomicReference<Exception> ex = new AtomicReference<Exception>();

		shell.open();

		ExecUtils.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						rt.get(50, TimeUnit.MILLISECONDS);
						break;
					} catch (TimeoutException e) {
					} catch (Exception e) {
						ex.set(e);
						break;
					}
				}
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						shell.close();
					}
				});
			}
		});

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}

		if (ex.get() == null) {
			System.out.println("Test resulted: " + rt.get());
		} else {
			throw ex.get();
		}
	}
}
