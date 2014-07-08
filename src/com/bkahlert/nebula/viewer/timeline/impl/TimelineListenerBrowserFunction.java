package com.bkahlert.nebula.viewer.timeline.impl;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public abstract class TimelineListenerBrowserFunction extends BrowserFunction {
	/**
	 * Extracts the band and event number from an arguments array such as
	 * ["3,13", ...].
	 * 
	 * @param id
	 * @return
	 */
	public static int[] fromArguments(Object[] arguments) {
		if (arguments.length == 1 && arguments[0] instanceof String) {
			String[] parts = ((String) arguments[0]).split(",");
			try {
				int bandGroupNumber = Integer.parseInt(parts[0]);
				int bandNumber = Integer.parseInt(parts[1]);
				int eventNumber = Integer.parseInt(parts[2]);
				return new int[] { bandGroupNumber, bandNumber, eventNumber };
			} catch (Exception e) {

			}
		}
		;
		return null;
	}

	public TimelineListenerBrowserFunction(Browser browser, String name) {
		super(browser, name);
	}

	@Override
	public Object function(Object[] arguments) {
		int[] id = fromArguments(arguments);
		if (id != null)
			call(id[0], id[1], id[2]);
		return null;
	}

	public abstract void call(int bandGroupNumber, int bandNumber,
			int eventNumber);
}