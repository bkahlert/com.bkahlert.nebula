package com.bkahlert.nebula.widgets.browser.extended.html;

/**
 * Abstractions of an HTML tag like &lt;a
 * href=&quot;http://bkahlert.com&quot;&gt;bkahlert.com&lt;/a&gt;.
 * 
 * @author bkahlert
 * 
 */
public interface IElement {

	/**
	 * Returns the HTML tag's name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Returns the attribute with the given name.
	 * 
	 * @param name
	 * @return null if attribute is not set
	 */
	public String getAttribute(String name);

	/**
	 * Returns the {@link IElement}'s css classes.
	 * 
	 * @return
	 */
	public String[] getClasses();

	/**
	 * Returns the {@link IElement}'s content, which is the portion between the
	 * opening and closing tag.
	 * 
	 * @return
	 */
	public String getContent();

	/**
	 * Returns html that represents this {@link IElement}.
	 * 
	 * @return
	 */
	public String toHtml();
}
