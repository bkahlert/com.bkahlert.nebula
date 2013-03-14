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
	 */
	public void ankerClicked(IAnker anker);

	/**
	 * This method is called if the user's mouse entered or left a
	 * {@link IAnker}.
	 * 
	 * @param anker
	 * @param entered
	 *            is true if the mouse entered the {@link IAnker} and false if
	 *            the mouse left it.
	 */
	public void ankerHovered(IAnker anker, boolean entered);
}
