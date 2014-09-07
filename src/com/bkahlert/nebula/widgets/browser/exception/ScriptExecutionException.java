package com.bkahlert.nebula.widgets.browser.exception;

import com.bkahlert.nebula.utils.StringUtils;

public class ScriptExecutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public ScriptExecutionException(String script) {
		super("Could not run script: " + script);
	}

	public ScriptExecutionException(String script, Throwable e) {
		super("Could not run script: " + StringUtils.shorten(script), e);
	}

}
