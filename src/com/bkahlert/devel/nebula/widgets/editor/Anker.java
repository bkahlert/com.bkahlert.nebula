package com.bkahlert.devel.nebula.widgets.editor;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;

public class Anker implements IAnker {

	private String href;
	private String[] classes;
	private String content;

	public Anker(Element anker) {
		if (!anker.tagName().equals("a"))
			throw new IllegalArgumentException(
					"The given element is no anker tag");
		this.href = anker.attr("href");
		this.classes = anker.attr("class") != null ? anker.attr("class").split(
				"\\s+") : null;
		this.content = anker.text();
	}

	public Anker(String href, String[] classes, String content) {
		super();
		this.href = href;
		this.classes = classes;
		this.content = content;
	}

	@Override
	public String getHref() {
		return this.href;
	}

	@Override
	public String[] getClasses() {
		return this.classes;
	}

	@Override
	public String getContent() {
		return this.content;
	}

	@Override
	public String toString() {
		return this.toHtml();
	}

	@Override
	public String toHtml() {
		return "<a href=\""
				+ (this.href != null ? this.href : "")
				+ "\" class=\""
				+ (this.classes != null ? StringUtils.join(this.classes, " ")
						: "") + "\">"
				+ (this.content != null ? this.content : "") + "</a>";
	}

}
