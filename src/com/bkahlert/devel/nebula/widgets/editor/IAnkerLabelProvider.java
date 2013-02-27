package com.bkahlert.devel.nebula.widgets.editor;

import com.bkahlert.devel.nebula.widgets.browser.IAnker;

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
	 * given {@link IAnker}.
	 * <p>
	 * Please note that only up to one {@link IAnkerLabelProvider} can be
	 * responsible. As soon as one {@link IAnkerLabelProvider} return
	 * responsibility the other registered {@link IAnkerLabelProvider}s are
	 * skipped for the given {@link IAnker}.
	 * 
	 * @param uri
	 * @return
	 */
	public boolean isResponsible(IAnker anker);

	public String getHref(IAnker anker);

	public String[] getClasses(IAnker anker);

	public String getContent(IAnker anker);
}
