package com.bkahlert.nebula.utils.replacement;

public class ReplacementException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReplacementException(Throwable throwable) {
		super(throwable);
	}

	public ReplacementException(String text, Throwable throwable) {
		super(text, throwable);
	}

}
