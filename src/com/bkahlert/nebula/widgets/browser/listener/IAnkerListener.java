package com.bkahlert.nebula.widgets.browser.listener;

import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

public interface IAnkerListener {
/**
	 * This method is called if an {@link IElement} was clicked.
	 * <p>
	 * <strong>Warning:</strong> Only {@link IElement#getHref()} returns a value.
	 * The other methods return null; FIXME: Fill other values
	 * <p><strong>REPLACED BY {@link IMouseListener#clicked(double, double, com.bkahlert.nebula.widgets.browser.extended.html.Element)</strong>
	 * 
	 * @param anker
	 * 
	 */
	@Deprecated
	public void ankerClicked(IAnker anker);

	/**
	 * This method is called if the user's mouse entered or left a
	 * {@link IElement}.
	 * 
	 * @param anker
	 * @param entered
	 *            is true if the mouse entered the {@link IElement} and false if
	 *            the mouse left it.
	 */
	public void ankerHovered(IAnker anker, boolean entered);
}
