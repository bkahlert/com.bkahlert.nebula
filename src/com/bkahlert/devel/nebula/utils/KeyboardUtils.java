package com.bkahlert.devel.nebula.utils;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IStartup;

public class KeyboardUtils implements IStartup {

	private static final Logger LOGGER = Logger.getLogger(KeyboardUtils.class);
	private static boolean metaKeyPressed = false;

	public static boolean isMetaKeyPressed() {
		return metaKeyPressed;
	}

	@Override
	public void earlyStartup() {
		final Listener metaKeyListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				metaKeyPressed = e.type == SWT.KeyDown
						&& ((e.stateMask & SWT.CTRL) != 0
								|| (e.stateMask & SWT.COMMAND) != 0
								|| (e.keyCode & SWT.CTRL) != 0 || (e.keyCode & SWT.COMMAND) != 0);
			}
		};
		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					Display.getDefault()
							.addFilter(SWT.KeyDown, metaKeyListener);
					Display.getDefault().addFilter(SWT.KeyUp, metaKeyListener);
					Display.getDefault()
							.addFilter(SWT.FocusIn, metaKeyListener);
					Display.getDefault().addFilter(SWT.FocusOut,
							metaKeyListener);
				}
			});
		} catch (Exception e1) {
			LOGGER.error(e1);
		}
	}

}
