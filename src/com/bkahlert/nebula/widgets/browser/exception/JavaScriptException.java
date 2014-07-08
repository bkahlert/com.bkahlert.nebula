package com.bkahlert.nebula.widgets.browser.exception;

public class JavaScriptException extends Exception {

	private static final long serialVersionUID = 1L;

	private String filename;
	private Long lineNumber;
	private String detail;

	public JavaScriptException(String filename, Long lineNumber, String detail) {
		super("JavaScript error occurred in " + filename + ":" + lineNumber
				+ "\n" + detail);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.detail = detail;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the lineNumber
	 */
	public Long getLineNumber() {
		return lineNumber;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

}
