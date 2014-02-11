package com.bkahlert.nebula.browser.exception;

import com.bkahlert.devel.nebula.widgets.browser.IJavaScript;

public class ScriptExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ScriptExecutionException(IJavaScript script) {
		super("Could not run script: " + script);
	}

	public ScriptExecutionException(IJavaScript script, Throwable e) {
		super("Could not run script: " + script, e);
	}

}
