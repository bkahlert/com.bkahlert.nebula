package com.bkahlert.nebula.widgets.browser.exception;

public class JavaScriptException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String script;
	private final String filename;
	private final Long lineNumber;
	private final Long columnNumber;
	private final String detail;

	public JavaScriptException(String script, String filename, Long lineNumber,
			Long columnNumber, String detail) {
		super("JavaScript error occurred"
				+ (filename != null ? " in " + filename
						: " at unknown location") + "\n\tLine: "
				+ (lineNumber != null ? lineNumber : "unknown")
				+ "\n\tColumn: "
				+ (columnNumber != null ? columnNumber : "unknown")
				+ "\n\tDetail: " + detail + "\n\tScript: " + script);
		this.script = script;
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.detail = detail;
	}

	public String getScript() {
		return this.script;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return the line number
	 */
	public Long getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * @return the column number
	 */
	public Long getColumnNumber() {
		return this.columnNumber;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return this.detail;
	}

}
