package com.bkahlert.nebula.widgets.browser.extended.extensions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.widgets.browser.IBrowser;

/**
 * This standard implementation of the {@link IBrowserExtension} can extend
 * {@link IBrowser}s with JavaScript.
 * <p>
 * The loading process depends on where the jsExtensions (.js) is located. If
 * the file is on the local machine it is loaded inline. Otherwise a
 * <code>script</code> tag is generated.
 * <p>
 * TODO Currently the loading of external ressources does not work reliably.
 * This can lead to waiting threads which are never notified.
 * 
 * @author bkahlert
 * 
 */
public class BrowserExtension implements IBrowserExtension {

	private final String name;
	private final String verificationScript;
	private final List<File> jsExtensions;
	private final List<URI> cssExtensions;

	private final List<Class<? extends IBrowserExtension>> dependencies;

	/**
	 * This constructor allows adding a multiple JS and CSS files.
	 * 
	 * @param name
	 * @param verificationScript
	 * @param jsExtensions
	 * @param arrayList
	 */
	public BrowserExtension(String name, String verificationScript,
			List<File> jsExtensions, List<URI> cssExtensions,
			ArrayList<Class<? extends IBrowserExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = Collections
				.unmodifiableList(jsExtensions != null ? new ArrayList<File>(
						jsExtensions) : new ArrayList<File>(0));
		this.cssExtensions = Collections
				.unmodifiableList(cssExtensions != null ? new ArrayList<URI>(
						cssExtensions) : new ArrayList<URI>(0));
		this.dependencies = Collections
				.unmodifiableList(dependencies != null ? new ArrayList<Class<? extends IBrowserExtension>>(
						dependencies)
						: new ArrayList<Class<? extends IBrowserExtension>>(0));
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getVerificationScript() {
		return this.verificationScript;
	}

	@Override
	public List<File> getJsExtensions() {
		return this.jsExtensions;
	}

	@Override
	public List<URI> getCssExtensions() {
		return this.cssExtensions;
	}

	@Override
	public List<Class<? extends IBrowserExtension>> getDependencies() {
		return this.dependencies;
	}

}
