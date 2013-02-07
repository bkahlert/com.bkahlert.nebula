package com.bkahlert.devel.nebula.widgets.browser;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.IWidget;

/**
 * Instances of this interface denote a {@link Composite} that is based on a
 * native {@link Browser}.
 * 
 * @author bkahlert
 * 
 */
public interface IBrowserComposite extends IWidget {
	/**
	 * Return the {@link Browser} used by this timeline.
	 * 
	 * @internal use of this method potentially dangerious since internal state
	 *           can transit to an inconsistent one
	 * @return must not return null (but may return an already disposed widget)
	 */
	public Browser getBrowser();
}