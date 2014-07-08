package com.bkahlert.nebula.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class SWTUtils {

	public static Point getMainScreenSize() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return new Point(width, height);
	}

	public static Point getDisplaySize() {
		try {
			return ExecUtils.syncExec(new Callable<Point>() {
				@Override
				public Point call() throws Exception {
					Rectangle bounds = Display.getCurrent().getBounds();
					return new Point(bounds.width, bounds.height);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Disposes all child {@link Control}s of the given {@link Composite}.
	 * 
	 * @param control
	 */
	public static void clearControl(Composite composite) {
		for (Control control : composite.getChildren()) {
			if (!control.isDisposed()) {
				control.dispose();
			}
		}
	}
}
