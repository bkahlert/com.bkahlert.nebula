package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;

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

	public static interface IConverter<T> {
		public T convert(Object returnValue);
	}

	public static final IConverter<Boolean> CONVERTER_BOOLEAN = new IConverter<Boolean>() {
		@Override
		public Boolean convert(Object returnValue) {
			if (returnValue == null || !Boolean.class.isInstance(returnValue)) {
				return false;
			}
			return (Boolean) returnValue;
		}
	};

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
	public Future<Boolean> open(URI uri, Integer timeout);

	/**
	 * This method is called from a non-UI thread before the
	 * {@link Browser#setUrl(String)} method is called.
	 * 
	 * @param uri
	 */
	public void beforeLoad(URI uri);

	/**
	 * This method is called from a non-UI thread after the
	 * {@link Browser#setUrl(String)} method has been called.
	 * 
	 * @param uri
	 */
	public void afterLoad(URI uri);

	/**
	 * This method is called from the-UI thread after the
	 * {@link IBrowserComposite} completed loading the page.
	 * 
	 * @param uri
	 * @return
	 */
	public Future<Void> afterCompletion(URI uri);

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
	public <T> Future<T> run(String script, IConverter<T> converter);

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

}