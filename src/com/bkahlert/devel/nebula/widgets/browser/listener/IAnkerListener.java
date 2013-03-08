package com.bkahlert.devel.nebula.widgets.browser.listener;

import com.bkahlert.devel.nebula.widgets.browser.IAnker;

public interface IAnkerListener {
	/**
	 * This method is called if an {@link IAnker} was clicked.
	 * <p>
	 * <strong>Warning:</strong> Only {@link IAnker#getHref()} returns a value.
	 * The other methods return null; FIXME: Fill other values
	 * 
	 * @param anker
	 * @param special
	 *            is true if while clicking the control ⌃ (Ctrl) or command ⌘
	 *            (Command) key was pressed
	 */
	public void ankerClicked(IAnker anker, boolean special);
}
