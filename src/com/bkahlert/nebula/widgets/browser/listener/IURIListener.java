package com.bkahlert.nebula.widgets.browser.listener;

import java.net.URI;

public interface IURIListener {
	/**
	 * This method is called if an {@link URI} was clicked.
	 * 
	 * @param uri
	 */
	public void uriClicked(URI uri);

	/**
	 * This method is called if the user's mouse entered of left a {@link URI}.
	 * 
	 * @param uri
	 * @param entered
	 *            is true if the mouse entered the {@link URI} and false if the
	 *            mouse left it.
	 */
	public void uriHovered(URI uri, boolean entered);
}
