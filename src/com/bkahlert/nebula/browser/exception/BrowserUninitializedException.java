package com.bkahlert.nebula.browser.exception;

import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;

public class BrowserUninitializedException extends Exception {

	private static final long serialVersionUID = 1L;

	public BrowserUninitializedException(IBrowserComposite browserComposite) {
		super("The " + browserComposite.getClass().getSimpleName()
				+ " has not been initialized, yet");
	}

}
