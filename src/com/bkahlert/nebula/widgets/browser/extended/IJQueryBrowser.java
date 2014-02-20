package com.bkahlert.nebula.widgets.browser.extended;

import java.util.concurrent.Future;

import org.eclipse.swt.graphics.Point;

import com.bkahlert.nebula.widgets.browser.IBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

public interface IJQueryBrowser extends IBrowser {

	/**
	 * Returns a {@link Future} that tells you if at least one element can be
	 * found with the given {@link ISelector}.
	 * 
	 * @param name
	 * @return
	 */
	public Future<Boolean> containsElements(ISelector selector);

	public Future<Point> getRelativePosition(ISelector selector);

	public Future<Point> getScrollPosition();

	public Future<Point> getScrollPosition(ISelector selector);

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
	 * Scrolls to the given element.
	 * 
	 * @param selector
	 * @return false if no scroll action was necessary
	 */
	public Future<Boolean> scrollTo(ISelector selector);

	/**
	 * Given the first element specified by the given selector the focus.
	 * 
	 * @param selector
	 * 
	 * @see <a href="http://api.jquery.com/focus/">api.jquery.com/focus/</a>
	 */
	public Future<Object> focus(ISelector selector);

	Future<IElement> getFocusedElement();

	/**
	 * Removes the focus from the first element specified by the given selector.
	 * 
	 * @param selector
	 * 
	 * @see <a href="http://api.jquery.com/focus/">api.jquery.com/blur/</a>
	 */
	public Future<Object> blur(ISelector selector);

	/**
	 * Triggers a key up event on the elements specified by the given selector.
	 * Typically only focused elements can be triggered.
	 * 
	 * @param selector
	 * 
	 * @see <a href="http://api.jquery.com/keyup/">api.jquery.com/keyup/</a>
	 */
	public Future<Object> keyUp(ISelector selector);

	/**
	 * Triggers a key down event on the elements specified by the given
	 * selector. Typically only focused elements can be triggered.
	 * 
	 * @param selector
	 * 
	 * @see <a href="http://api.jquery.com/keydown/">api.jquery.com/keydown/</a>
	 */
	public Future<Object> keyDown(ISelector selector);

	/**
	 * Triggers a key press event on the elements specified by the given
	 * selector. Typically only focused elements can be triggered.
	 * 
	 * @param selector
	 * 
	 * @see <a
	 *      href="http://api.jquery.com/keypress/">api.jquery.com/keypress/</a>
	 */
	public Future<Object> keyPress(ISelector selector);

	/**
	 * Triggers a key press event on the elements specified by the given
	 * selector. Typically only focused elements can be triggered.
	 * <p>
	 * In contrast to {@link #keyPress(ISelector, String)} this method call also
	 * triggers similar events to increase the chance that the web application
	 * reacts correctly. An example are input fields that trigger actions on key
	 * presses-like events where the actually used trigger is unknown.
	 * 
	 * @param selector
	 * 
	 * @see <a href="http://api.jquery.com/val/">api.jquery.com/val/</a>
	 */
	public Future<Object> forceKeyPress(ISelector selector);

	/**
	 * Not only sets a field's content but tries to simulate an actual user
	 * typing something in a input field.
	 * 
	 * @param selector
	 * @param text
	 * @return
	 */
	public Future<Object> simulateTyping(ISelector selector, String text);

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