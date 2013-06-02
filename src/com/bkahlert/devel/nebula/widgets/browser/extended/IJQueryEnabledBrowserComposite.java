package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.util.concurrent.Future;

import org.eclipse.swt.graphics.Point;

import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;

public interface IJQueryEnabledBrowserComposite extends IBrowserComposite {

	/**
	 * Returns a {@link Future} that tells you if at least one element can be
	 * found with the given {@link ISelector}.
	 * 
	 * @param name
	 * @return
	 */
	public Future<Boolean> containsElements(ISelector selector);

	/**
	 * Scrolls to the given position.
	 * 
	 * @param x
	 * @param y
	 * @return false if no scroll action was necessary
	 */
	public Future<Boolean> scrollTo(final int x, final int y);

	/**
	 * Scrolls to the given position.
	 * 
	 * @param pos
	 * @return false if no scroll action was necessary
	 */
	public Future<Boolean> scrollTo(Point pos);

	/**
	 * Sets the given value of the elements specified by the given selector.
	 * 
	 * @param selector
	 * @param value
	 * 
	 * @see <a href="http://api.jquery.com/val/">api.jquery.com/val/</a>
	 */
	public Future<Object> val(ISelector selector, String value);

	/**
	 * Submits the form belonging to the element(s) specified by the given
	 * selector.
	 * 
	 * @param selector
	 * @return
	 */
	public Future<Object> submit(ISelector selector);

}