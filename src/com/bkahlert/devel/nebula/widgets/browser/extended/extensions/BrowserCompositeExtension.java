package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite.IConverter;
import com.bkahlert.nebula.utils.CompletedFuture;

/**
 * This standard implementation of the {@link IBrowserCompositeExtension} can
 * extend {@link IBrowserComposite}s with JavaScript.
 * <p>
 * The loading process depends on where the extension (.js) is located. If the
 * file is on the local machine it is loaded inline. Otherwise a
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

	private String name;
	private String verificationScript;
	private URI extension;

	private List<Class<? extends IBrowserCompositeExtension>> dependencies;

	public BrowserCompositeExtension(String name, String verificationScript,
			URI extension,
			ArrayList<Class<? extends IBrowserCompositeExtension>> arrayList) {
		Assert.isLegal(name != null && verificationScript != null
				&& extension != null);
		this.name = name;
		this.verificationScript = verificationScript;
		this.extension = extension;
		this.dependencies = arrayList;
	}

	public BrowserCompositeExtension(String name, String verificationScript,
			URI extension) {
		this(name, verificationScript, extension, null);
	}

	@Override
	public Future<Boolean> hasExtension(IBrowserComposite browserComposite) {
		try {
			return browserComposite.run(this.verificationScript,
					IBrowserComposite.CONVERTER_BOOLEAN);
		} catch (Exception e) {
			LOGGER.error("Could not verify the existence of the extension \""
					+ this.name + "\".", e);
			return new CompletedFuture<Boolean>(false);
		}
	}

	@Override
	public Future<Boolean> addExtension(final IBrowserComposite browserComposite) {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
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

				if ("file"
						.equalsIgnoreCase(BrowserCompositeExtension.this.extension
								.getScheme())) {
					File file = new File(
							BrowserCompositeExtension.this.extension.toString()
									.substring("file://".length()));
					String script = FileUtils.readFileToString(file);
					return browserComposite.run(script,
							new IConverter<Boolean>() {
								@Override
								public Boolean convert(Object returnValue) {
									return true;
								}
							}).get();
				} else {
					try {
						return browserComposite.run(
								BrowserCompositeExtension.this.extension).get();
					} catch (Exception e) {
						LOGGER.error("Could not load the extension \""
								+ BrowserCompositeExtension.this.name + "\".",
								e);
						return null;
					}
				}
			}
		});
	}

	@Override
	public Future<Boolean> addExtensionOnce(
			final IBrowserComposite browserComposite) {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
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

}
