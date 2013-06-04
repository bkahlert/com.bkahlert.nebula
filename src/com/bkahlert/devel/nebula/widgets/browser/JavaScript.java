package com.bkahlert.devel.nebula.widgets.browser;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public class JavaScript implements IJavaScript {

	public static final String STATEMENT_SEPARATOR = ";";

	private String script;

	public JavaScript(String script) {
		this.script = script;
	}

	public JavaScript(IJavaScript... scripts) {
		this(StringUtils.join(scripts, STATEMENT_SEPARATOR));
	}

	public JavaScript(List<IJavaScript> scripts) {
		this(StringUtils.join(scripts, STATEMENT_SEPARATOR));
	}

	@Override
	public String toString() {
		return this.script;
	};

}
