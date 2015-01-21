package com.bkahlert.nebula.widgets.browser.runner;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.swt.browser.Browser;

import com.bkahlert.nebula.utils.IConverter;

/**
 * Implementations of this script can execute {@link IJavaScript}s using a
 * {@link Browser}.
 *
 * @author bkahlert
 *
 */
public interface IBrowserScriptRunner {

	/**
	 * Injects the script addressed by the given {@link URI} and returns a
	 * {@link Future} that blocks until the script is completely loaded.
	 * <p>
	 * In contrast to {@link #run(URI)} the reference (
	 * <code>&lt;script src="..."&gt&lt;/script&gt</code>) is kept.
	 *
	 * @param script
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Boolean> inject(URI script);

	/**
	 * Runs the script contained in the given {@link File} in the browser as
	 * soon as its content is loaded.
	 *
	 * @param script
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Boolean> run(File script);

	/**
	 * Runs the script contained in the given {@link URI} in the browser as soon
	 * as its content is loaded.
	 * <p>
	 * In contrast to {@link #inject(URI)} functionality made available through
	 * the script does not persist. To inject script libraries like jQuery
	 * {@link #inject(URI)} is recommended. <b>Exception: If the resource is
	 * actually a file on the local file system, its content will be run and
	 * therefore persist to circumvent security restrictions.
	 *
	 * @param script
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Boolean> run(URI script);

	/**
	 * Runs the given script in the browser as soon as its content is loaded and
	 * returns the evaluation's return value.
	 *
	 * @param script
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public Future<Object> run(String script);

	/**
	 * Runs the given script in a debouncing fashion. Consecutive calls are
	 * script calls with the same scope and less than than <code>interval</code>
	 * milliseconds between them.
	 *
	 * @param script
	 * @param interval
	 * @param scope
	 */
	public void run(String script, long interval, String scope);

	/**
	 * Runs the given script in the browser as soon as its content is loaded and
	 * returns the evaluation's converted return value.
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
	 * Runs the script <b>contained</b> in the given {@link File} in the browser
	 * immediately. This means the file is not linked but its content is read
	 * and directly executed.
	 *
	 * @param scriptFile
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */

	public void runContentsImmediately(File scriptFile) throws Exception;

	/**
	 * Runs the script <b>contained</b> in the given {@link File} in the browser
	 * immediately. This means the file is not linked but its content is
	 * directly put into a <code>script</code> tag.
	 *
	 * @param scriptFile
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	void runContentsAsScriptTagImmediately(File scriptFile) throws Exception;

	/**
	 * Runs the given script in the browser immediately and returns the
	 * evaluation's converted return value.
	 *
	 * @param script
	 * @param converter
	 * @return
	 *
	 * @ArbitraryThread may be called from whatever thread.
	 */
	public <DEST> DEST runImmediately(String script,
			IConverter<Object, DEST> converter) throws Exception;

	/**
	 * Gets called if when the given script is about to be executed by the
	 * browser.
	 *
	 * @param script
	 */
	public void scriptAboutToBeSentToBrowser(String script);

	/**
	 * Gets called when the previously executed script finished execution.
	 *
	 * @param returnValue
	 */
	public void scriptReturnValueReceived(Object returnValue);

}
