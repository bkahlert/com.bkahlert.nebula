package com.bkahlert.nebula.widgets.browser.extended.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;

public class Element implements IElement {

	private final String name;
	private Map<String, String> attributes = new HashMap<String, String>();
	private final String[] classes;
	private final String content;

	public Element(String name, Map<String, String> attributes, String content) {
		this.name = name;
		this.attributes.putAll(attributes);
		this.classes = attributes.containsKey("class") ? attributes
				.get("class").split("\\s+") : null;
		this.attributes.remove("class");
		this.content = content;
	}

	public Element(org.jsoup.nodes.Element element) {
		this.name = element.tagName();
		Map<String, String> attributes = new HashMap<String, String>();
		for (Attribute attribute : element.attributes()) {
			attributes.put(attribute.getKey(), attribute.getValue());
		}
		this.attributes = attributes;
		this.classes = element.attr("class") != null ? element.attr("class")
				.split("\\s+") : null;
		this.attributes.remove("class");
		this.content = element.text();
	}

	public Element(String html) {
		this(Jsoup.parseBodyFragment(html).body().child(0));
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getAttribute(String name) {
		return this.attributes.get(name);
	}

	@Override
	public String[] getClasses() {
		return this.classes;
	}

	@Override
	public String getData(String key) {
		return this.getAttribute("data-" + key);
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
		StringBuilder attrHtml = new StringBuilder();
		for (Entry<String, String> attr : this.attributes.entrySet()) {
			String key = attr.getKey();
			String value = attr.getValue() != null ? attr.getValue() : "";
			attrHtml.append(" " + key + "=");
			attrHtml.append("\"" + value + "\"");
		}
		return "<"
				+ this.name
				+ attrHtml.toString()
				+ " class=\""
				+ (this.classes != null ? StringUtils.join(this.classes, " ")
						: "") + "\">"
				+ (this.content != null ? this.content : "") + "</" + this.name
				+ ">";
	}

}
