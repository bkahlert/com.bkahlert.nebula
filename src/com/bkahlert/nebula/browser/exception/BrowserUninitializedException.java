package com.bkahlert.nebula.browser.exception;

import org.eclipse.swt.browser.Browser;

public class BrowserUninitializedException extends Exception {

	private static final long serialVersionUID = 1L;

	public BrowserUninitializedException(Browser browser) {
		super("The " + browser.getClass().getSimpleName()
				+ " has not been initialized, yet");
	}

}
