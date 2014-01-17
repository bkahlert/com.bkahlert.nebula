package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import java.util.concurrent.Future;

import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.ExtendedBrowserComposite;

/**
 * Interface for extension that can be used with
 * {@link ExtendedBrowserComposite}s.
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
	 */
	public Future<Boolean> hasExtension(
			IBrowserComposite extendedBrowserComposite);

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
			IBrowserComposite extendedBrowserComposite);

	/**
	 * Loads this extension if not already loaded.
	 * 
	 * @param extendedBrowserComposite
	 * @return
	 */
	public Future<Boolean> addExtensionOnce(
			IBrowserComposite extendedBrowserComposite);

}