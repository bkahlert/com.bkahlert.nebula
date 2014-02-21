package com.bkahlert.nebula.widgets.browser;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.IWidget;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.browser.runner.IBrowserScriptRunner;

/**
 * Instances of this interface denote a {@link Composite} that is based on a
 * native {@link Browser}.
 * 
 * @author bkahlert
 * 
 */
public interface IBrowser extends IBrowserScriptRunner, IWidget {

	/**
	 * Return the {@link Browser} used by this timeline.
	 * 
	 * @internal use of this method potentially dangerious since internal state
	 *           can transit to an inconsistent one
	 * @return must not return null (but may return an already disposed widget)
	 */
	public Browser getBrowser();

	/**
	 * Opens the given address.
	 * 
	 * @param address
	 * @param timeout
	 *            after which the {@link IBrowser} stops loading
	 * @return true if page could be successfully loaded; false if the timeout
	 *         was reached
	 */
	public Future<Boolean> open(String address, Integer timeout);

	/**
	 * Opens the given address.
	 * 
	 * @param address
	 * @param timeout
	 *            after which the {@link IBrowser} stops loading
	 * @pageLoadCheckScript that must return true if the page correctly loaded.
	 *                      This is especially useful if some inner page setup
	 *                      takes place.
	 * @return true if page could be successfully loaded; false if the timeout
	 *         was reached
	 */
	public Future<Boolean> open(String address, Integer timeout,
			String pageLoadCheckScript);

	/**
	 * Opens the given address.
	 * 
	 * @param address
	 * @param timeout
	 *            after which the {@link IBrowser} stops loading
	 * @return true if page could be successfully loaded; false if the timeout
	 *         was reached
	 */
	public Future<Boolean> open(URI uri, Integer timeout);

	/**
	 * Opens the given address.
	 * 
	 * @param address
	 * @param timeout
	 *            after which the {@link IBrowser} stops loading
	 * @pageLoadCheckScript that must return true if the page correctly loaded.
	 *                      This is especially useful if some inner page setup
	 *                      takes place.
	 * @return true if page could be successfully loaded; false if the timeout
	 *         was reached
	 */
	public Future<Boolean> open(URI uri, Integer timeout,
			String pageLoadCheckScript);

	/**
	 * Opens a blank page
	 * 
	 * @return true if page could be successfully loaded; false if an error
	 *         occurred
	 */
	public Future<Boolean> openAboutBlank();

	/**
	 * Sets if {@link IBrowser} may change its location by actions now invoked
	 * by {@link #open(String, Integer)}.
	 * 
	 * @param allow
	 */
	public void setAllowLocationChange(boolean allow);

	/**
	 * This method is called from a non-UI thread before the
	 * {@link Browser#setUrl(String)} method is called.
	 * 
	 * @param uri
	 */
	public void beforeLoad(String uri);

	/**
	 * This method is called from a non-UI thread after the
	 * {@link Browser#setUrl(String)} method has been called.
	 * 
	 * @param uri
	 */
	public void afterLoad(String uri);

	/**
	 * This method is called from the-UI thread after the {@link IBrowser}
	 * completed loading the page.
	 * 
	 * @param uri
	 * @return
	 */
	public Future<Void> beforeCompletion(String uri);

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 * @return
	 */
	public Future<Void> injectCssFile(URI uri);

	/**
	 * Adds the given CSS code to the head.
	 * 
	 * @return
	 */
	public Future<Void> injectCss(String css);

	/**
	 * Returns a {@link Future} that tells you if an element with the given id
	 * exists.
	 * 
	 * @param id
	 * @return
	 */
	public Future<Boolean> containsElementWithID(String id);

	/**
	 * Returns a {@link Future} that tells you if at least one element with the
	 * given name exists.
	 * 
	 * @param name
	 * @return
	 */
	public Future<Boolean> containsElementsWithName(String name);

	public void addAnkerListener(IAnkerListener ankerListener);

	public void removeAnkerListener(IAnkerListener ankerListener);

	public void addFocusListener(IFocusListener focusListener);

	public void removeFocusListener(IFocusListener focusListener);

	/**
	 * Sets the body's inner HTML.
	 * 
	 * @param html
	 * @return
	 */
	public Future<Void> setBodyHtml(String html);

	/**
	 * Returns the body's inner HTML.
	 * 
	 * @return
	 */
	public Future<String> getBodyHtml();

	/**
	 * Returns the document's HTML
	 */
	public Future<String> getHtml();
}