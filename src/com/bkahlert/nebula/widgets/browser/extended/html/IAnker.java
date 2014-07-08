package com.bkahlert.nebula.widgets.browser.extended.html;


/**
 * Abstractions of an anker tag like &lt;a
 * href=&quot;http://bkahlert.com&quot;&gt;bkahlert.com&lt;/a&gt;.
 * 
 * @author bkahlert
 * 
 */
public interface IAnker extends IElement {

	/**
	 * Returns the {@link IElement}'s href attribute.
	 * 
	 * @return
	 */
	public String getHref();

}
