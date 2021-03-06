package com.bkahlert.nebula.widgets.composer;

import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;

/**
 * Instances of this class provide labels for existing anker tags (e.g. &lt;a
 * href=&quot;http://bkahlert.com&quot;&gt;bkahlert.com&lt;/a&gt;).
 * 
 * @author bkahlert
 * 
 */
public interface IAnkerLabelProvider {
	/**
	 * Returns true if this {@link IAnkerLabelProvider} is responsible for the
	 * given {@link IElement}.
	 * <p>
	 * Please note that only up to one {@link IAnkerLabelProvider} can be
	 * responsible. As soon as one {@link IAnkerLabelProvider} return
	 * responsibility the other registered {@link IAnkerLabelProvider}s are
	 * skipped for the given {@link IElement}.
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isResponsible(IAnker anker);

	public String getHref(IAnker anker);

	public String[] getClasses(IAnker anker);

	public String getContent(IAnker anker);
}
