package com.bkahlert.devel.nebula.widgets.browser.listener;

import java.net.URI;

public interface IURIListener {
	/**
	 * This method is called if an {@link URI} was clicked.
	 * 
	 * @param uri
	 * @param special
	 *            is true if while clicking the control ⌃ (Ctrl) or command ⌘
	 *            (Command) key was pressed
	 */
	public void uriClicked(URI uri, boolean special);
}
