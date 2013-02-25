package com.bkahlert.devel.nebula.widgets.editor;

/**
 * Abstractions of an anker tag like &lt;a
 * href=&quot;http://bkahlert.com&quot;&gt;bkahlert.com&lt;/a&gt;.
 * 
 * @author bkahlert
 * 
 */
public interface IAnker {
	/**
	 * Returns the {@link IAnker}'s href attribute.
	 * 
	 * @return
	 */
	public String getHref();

	/**
	 * Returns the {@link IAnker}'s css classes.
	 * 
	 * @return
	 */
	public String[] getClasses();

	/**
	 * Returns the {@link IAnker}'s content, which is the portion between the
	 * opening and closing tag.
	 * 
	 * @return
	 */
	public String getContent();

	/**
	 * Returns html that represents this {@link IAnker}.
	 * 
	 * @return
	 */
	public String toHtml();
}
