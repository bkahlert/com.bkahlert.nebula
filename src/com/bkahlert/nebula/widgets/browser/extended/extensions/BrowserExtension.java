package com.bkahlert.nebula.widgets.browser.extended.extensions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.IBrowser;

/**
 * This standard implementation of the {@link IBrowserExtension} can
 * extend {@link IBrowser}s with JavaScript.
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

	private static final Logger LOGGER = Logger
			.getLogger(BrowserExtension.class);

	private final String name;
	private final String verificationScript;
	private final File[] jsExtensions;
	private final URI[] cssExtensions;

	private final List<Class<? extends IBrowserExtension>> dependencies;

	/**
	 * This constructor allows adding a single JS file.
	 * 
	 * @param name
	 * @param verificationScript
	 * @param jsExtensions
	 * @param arrayList
	 */
	public BrowserExtension(String name, String verificationScript,
			File jsExtension,
			ArrayList<Class<? extends IBrowserExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtension != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = new File[] { jsExtension };
		this.cssExtensions = new URI[0];
		this.dependencies = dependencies;
	}

	/**
	 * This constructor allows adding a single JS and a single CSS file.
	 * 
	 * @param name
	 * @param verificationScript
	 * @param jsExtensions
	 * @param arrayList
	 */
	public BrowserExtension(String name, String verificationScript,
			File jsExtension, URI cssExtension,
			ArrayList<Class<? extends IBrowserExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtension != null && cssExtension != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = new File[] { jsExtension };
		this.cssExtensions = new URI[] { cssExtension };
		this.dependencies = dependencies;
	}

	/**
	 * This constructor allows adding a multiple JS and CSS files.
	 * 
	 * @param name
	 * @param verificationScript
	 * @param jsExtensions
	 * @param arrayList
	 */
	public BrowserExtension(String name, String verificationScript,
			File[] jsExtensions, URI[] cssExtensions,
			ArrayList<Class<? extends IBrowserExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtensions != null && cssExtensions != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = jsExtensions;
		this.cssExtensions = cssExtensions;
		this.dependencies = dependencies;
	}

	@Override
	public Boolean hasExtension(IBrowser browser)
			throws Exception {
		return browser.runImmediately(this.verificationScript,
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Boolean> addExtension(final IBrowser browser) {
		return ExecUtils.nonUIAsyncExec(BrowserExtension.class,
				"Adding Extension", new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						if (BrowserExtension.this.dependencies != null) {
							for (Class<? extends IBrowserExtension> dependencyClass : BrowserExtension.this.dependencies) {
								try {
									IBrowserExtension dependency = dependencyClass
											.newInstance();
									dependency.addExtensionOnce(
											browser).get();
								} catch (Exception e) {
									LOGGER.warn(
											"Cannot instantiate dependency "
													+ dependencyClass
															.getSimpleName()
													+ ". Skipping.", e);
								}
							}
						}

						boolean success = true;
						for (File jsExtension : BrowserExtension.this.jsExtensions) {
							try {
								browser.runImmediately(jsExtension);
							} catch (Exception e) {
								LOGGER.error(
										"Could not load the JS extension \""
												+ BrowserExtension.this.name
												+ "\".", e);
								success = false;
							}
						}

						for (URI cssExtension : BrowserExtension.this.cssExtensions) {
							browser.injectCssFile(cssExtension);
						}

						return success;
					}
				});
	}

	@Override
	public Future<Boolean> addExtensionOnce(
			final IBrowser browser) {
		return ExecUtils.nonUIAsyncExec(BrowserExtension.class,
				"Adding Extension Once", new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						if (!BrowserExtension.this
								.hasExtension(browser)) {
							return BrowserExtension.this.addExtension(
									browser).get();
						}
						return null;
					}
				});
	}

}
