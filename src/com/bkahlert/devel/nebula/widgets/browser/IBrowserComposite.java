package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
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

	/**
	 * Return the {@link Browser} used by this timeline.
	 * 
	 * @internal use of this method potentially dangerious since internal state
	 *           can transit to an inconsistent one
	 * @return must not return null (but may return an already disposed widget)
	 */
	public Browser getBrowser();

	void injectCssFile(String path);

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
}