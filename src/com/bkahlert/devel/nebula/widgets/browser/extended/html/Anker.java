package com.bkahlert.devel.nebula.widgets.browser.extended.html;

import java.util.HashMap;
import java.util.Map;


public class Anker extends Element implements IAnker {

	public Anker(Map<String, String> attributes, String content) {
		super("a", attributes, content);
	}

	public Anker(org.jsoup.nodes.Element anker) {
		super(anker);
		if (!anker.tagName().equals("a")) {
			throw new IllegalArgumentException(
					"The given element is no anker tag");
		}
	}

	@SuppressWarnings("serial")
	public Anker(final String href, final String[] classes, String content) {
		super("a", new HashMap<String, String>() {
			{
				if (href != null) {
					this.put("href", href);
				}
				if (classes != null) {
					this.put("class", org.apache.commons.lang.StringUtils.join(
							classes, " "));
				}
			}
		}, content);
	}

	@Override
	public String getHref() {
		return this.getAttribute("href");
	}

}
