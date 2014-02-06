package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.widgets.IWidget;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;

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

	/**
	 * Opens the given {@link URI}.
	 * 
	 * @param uri
	 * @param timeout
	 *            after which the {@link IBrowserComposite} stops loading and
	 *            throws an exception.
	 * @return
	 */
	public Future<Boolean> open(String uri, Integer timeout);

	public Future<Boolean> open(URI uri, Integer timeout);

	public Future<Boolean> openAboutBlank();

	/**
	 * Sets if {@link IBrowserComposite} may change its location by actions now
	 * invoked by {@link #open(String, Integer)}.
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
	 * This method is called from the-UI thread after the
	 * {@link IBrowserComposite} completed loading the page.
	 * 
	 * @param uri
	 * @return
	 */
	public Future<Void> afterCompletion(String uri);

	/**
	 * Injects the given script and returns a {@link Future} that blocks until
	 * the script is completely loaded.
	 * 
	 * @param script
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Boolean> inject(URI script);

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 */
	public void injectCssFile(URI uri);

	/**
	 * Adds the given CSS code to the head.
	 */
	public void injectCss(String css);

	/**
	 * Runs the script included in the given {@link File} in the
	 * {@link IBrowserComposite} as soon as its content is loaded.
	 * 
	 * @param script
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public void run(File script);

	/**
	 * Runs the script included in the given {@link URI} in the
	 * {@link IBrowserComposite} as soon as its content is loaded.
	 * 
	 * @param script
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Boolean> run(URI script);

	/**
	 * Runs the given script in the {@link IBrowserComposite} as soon as its
	 * content is loaded and returns the {@link IBrowserComposite}'s return
	 * value.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param script
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Object> run(String script);

	/**
	 * Runs the given script in the {@link IBrowserComposite} as soon as its
	 * content is loaded and returns the {@link IBrowserComposite}'s return
	 * value.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param script
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Object> run(IJavaScript script);

	/**
	 * Runs the given script in the {@link IBrowserComposite} as soon as its
	 * content is loaded and returns the {@link IBrowserComposite}'s converted
	 * return value.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param script
	 * @param converter
	 * @return
	 * 
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public <DEST> Future<DEST> run(String script,
			IConverter<Object, DEST> converter);

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

}