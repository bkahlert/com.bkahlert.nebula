package com.bkahlert.nebula.widgets.browser.extended.extensions;

import java.io.File;
import java.net.URI;
import java.util.List;

import com.bkahlert.nebula.widgets.browser.IBrowser;
import com.bkahlert.nebula.widgets.browser.extended.ExtendedBrowser;

/**
 * Interface for extension that can be used with {@link ExtendedBrowser}s.
 * <p>
 * Examples are jQuery and Bootstrap.
 * 
 * @author bkahlert
 * 
 */
public interface IBrowserExtension {

	/**
	 * Returns this extension's name.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Returns the script that must return a boolean which tells if the
	 * {@link IBrowser} had loaded the extensions successfully.
	 * 
	 * @return
	 */
	public String getVerificationScript();

	/**
	 * Returns the paths to the JS files that make up this extension.
	 * 
	 * @return immutable list that is never <code>null</code>
	 */
	public List<File> getJsExtensions();

	/**
	 * Returns the paths to the CSS files that make up this extension.
	 * 
	 * @return immutable list that is never <code>null</code>
	 */
	public List<URI> getCssExtensions();

	/**
	 * Returns the {@link IBrowserExtension}s this extension requires.
	 * 
	 * @return immutable list that is never <code>null</code>
	 */
	public List<Class<? extends IBrowserExtension>> getDependencies();

}