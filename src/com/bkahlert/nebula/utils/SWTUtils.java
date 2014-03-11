package com.bkahlert.nebula.utils;

import java.util.concurrent.Callable;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class SWTUtils {
	public static Rectangle getDisplayBounds() {
		try {
			return ExecUtils.syncExec(new Callable<Rectangle>() {
				@Override
				public Rectangle call() throws Exception {
					return Display.getCurrent().getBounds();
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
