package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.nebula.utils.CompletedFuture;

/**
 * This standard implementation of the {@link IBrowserCompositeExtension} can
 * extend {@link IBrowserComposite}s with JavaScript.
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
public class BrowserCompositeExtension implements IBrowserCompositeExtension {

	private static final Logger LOGGER = Logger
			.getLogger(BrowserCompositeExtension.class);

	private final ExecutorUtil executorUtil = new ExecutorUtil(
			BrowserCompositeExtension.class);

	private final String name;
	private final String verificationScript;
	private final URI[] jsExtensions;
	private final URI[] cssExtensions;

	private final List<Class<? extends IBrowserCompositeExtension>> dependencies;

	/**
	 * This constructor allows adding a single JS file.
	 * 
	 * @param name
	 * @param verificationScript
	 * @param jsExtensions
	 * @param arrayList
	 */
	public BrowserCompositeExtension(String name, String verificationScript,
			URI jsExtension,
			ArrayList<Class<? extends IBrowserCompositeExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtension != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = new URI[] { jsExtension };
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
	public BrowserCompositeExtension(String name, String verificationScript,
			URI jsExtension, URI cssExtension,
			ArrayList<Class<? extends IBrowserCompositeExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtension != null && cssExtension != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = new URI[] { jsExtension };
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
	public BrowserCompositeExtension(String name, String verificationScript,
			URI[] jsExtensions, URI[] cssExtensions,
			ArrayList<Class<? extends IBrowserCompositeExtension>> dependencies) {
		Assert.isLegal(name != null && verificationScript != null
				&& jsExtensions != null && cssExtensions != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.jsExtensions = jsExtensions;
		this.cssExtensions = cssExtensions;
		this.dependencies = dependencies;
	}

	@Override
	public Future<Boolean> hasExtension(IBrowserComposite browserComposite) {
		try {
			return browserComposite.run(this.verificationScript,
					IConverter.CONVERTER_BOOLEAN);
		} catch (Exception e) {
			return new CompletedFuture<Boolean>(false, e);
		}
	}

	@Override
	public Future<Boolean> addExtension(final IBrowserComposite browserComposite) {
		return executorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				if (BrowserCompositeExtension.this.dependencies != null) {
					for (Class<? extends IBrowserCompositeExtension> dependencyClass : BrowserCompositeExtension.this.dependencies) {
						try {
							IBrowserCompositeExtension dependency = dependencyClass
									.newInstance();
							dependency.addExtensionOnce(browserComposite).get();
						} catch (Exception e) {
							LOGGER.warn("Cannot instantiate dependency "
									+ dependencyClass.getSimpleName()
									+ ". Skipping.", e);
						}
					}
				}

				boolean success = true;
				for (URI jsExtension : BrowserCompositeExtension.this.jsExtensions) {
					if (!inject(browserComposite, jsExtension,
							BrowserCompositeExtension.this.name)) {
						success = false;
					}
				}

				for (URI cssExtension : BrowserCompositeExtension.this.cssExtensions) {
					browserComposite.injectCssFile(cssExtension);
				}

				return success;
			}
		});
	}

	@Override
	public Future<Boolean> addExtensionOnce(
			final IBrowserComposite browserComposite) {
		return executorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				if (!BrowserCompositeExtension.this.hasExtension(
						browserComposite).get()) {
					return BrowserCompositeExtension.this.addExtension(
							browserComposite).get();
				}
				return null;
			}
		});
	}

	/*
	 * TODO merge with BrowserComposite inject (only difference here: directly
	 * injects if local file)
	 */
	private static Boolean inject(IBrowserComposite browserComposite,
			URI jsExtension, String name) throws IOException,
			InterruptedException, ExecutionException {
		if ("file".equalsIgnoreCase(jsExtension.getScheme())) {
			File file = new File(jsExtension.toString().substring(
					"file://".length()));
			String script = FileUtils.readFileToString(file);
			return browserComposite.run(script,
					new IConverter<Object, Boolean>() {
						@Override
						public Boolean convert(Object returnValue) {
							return true;
						}
					}).get();
		} else {
			try {
				return browserComposite.inject(jsExtension).get();
			} catch (Exception e) {
				LOGGER.error("Could not load the JS extension \"" + name
						+ "\".", e);
				return false;
			}
		}
	}

}
