package com.bkahlert.nebula.widgets.browser.extended.extensions;

import java.util.concurrent.Future;

import com.bkahlert.nebula.widgets.browser.IBrowser;
import com.bkahlert.nebula.widgets.browser.extended.ExtendedBrowser;

/**
 * Interface for extension that can be used with
 * {@link ExtendedBrowser}s.
 * <p>
 * Examples are jQuery and Bootstrap.
 * 
 * @author bkahlert
 * 
 */
public interface IBrowserCompositeExtension {

	/**
	 * Checks if this extension is already loaded.
	 * 
	 * @param extendedBrowserComposite
	 * @return true if this extension is already loaded.
	 * @throws Exception
	 */
	public Boolean hasExtension(IBrowser extendedBrowserComposite)
			throws Exception;

	/**
	 * Loads this extension independently of its current loading state.
	 * <p>
	 * Check {@link #hasExtension(IExtendedBrowserComposite)} if you don't want
	 * to load this extension twice or use
	 * {@link #addExtensionOnce(IExtendedBrowserComposite)}.
	 * 
	 * @param extendedBrowserComposite
	 * @return
	 */
	public Future<Boolean> addExtension(
			IBrowser extendedBrowserComposite);

	/**
	 * Loads this extension if not already loaded.
	 * 
	 * @param extendedBrowserComposite
	 * @return
	 */
	public Future<Boolean> addExtensionOnce(
			IBrowser extendedBrowserComposite);

}