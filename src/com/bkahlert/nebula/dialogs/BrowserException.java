package com.bkahlert.nebula.dialogs;

public class BrowserException extends Exception {

	private static final long serialVersionUID = 1L;

	public BrowserException(String message, Throwable innerException) {
		super(message, innerException);
	}

	public BrowserException(Throwable innerException) {
		super(innerException);
	}

	public BrowserException(String string) {
		super(string);
	}

}
