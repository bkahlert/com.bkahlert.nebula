package com.bkahlert.nebula.widgets.browser.extended;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.IBrowser;
import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;

/**
 * This {@link IBrowser} behaves like the {@link Browser} but allows
 * {@link IBrowserExtension}s to be automatically loaded when the requested
 * {@link URI} was loaded.
 * 
 * @author bkahlert
 * 
 */
public class ExtendedBrowser extends Browser implements IBrowser {

	private static final Logger LOGGER = Logger
			.getLogger(ExtendedBrowser.class);

	private final IBrowserExtension[] extensions;

	public ExtendedBrowser(Composite parent, int style,
			IBrowserExtension[] extensions) {
		super(parent, style);
		this.extensions = extensions;
	}

	@Override
	public Future<Void> beforeCompletion(String uri) {
		/*
		 * TODO FIX BUG: afterCompletion is called after the DOMReady scripts.
		 * PageLoad might need to access something loaded through extensions.
		 */
		return ExecUtils.asyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				for (IBrowserExtension extension : ExtendedBrowser.this.extensions) {
					try {
						if (!ExtendedBrowser.this.addExtensionOnce(extension)) {
							LOGGER.error("Error loading " + extension);
						}
					} catch (Exception e) {
						LOGGER.error(e);
					}
				}
				return null;
			}
		});
	}

	private Boolean hasExtension(IBrowserExtension extension) throws Exception {
		return this.runImmediately(extension.getVerificationScript(),
				IConverter.CONVERTER_BOOLEAN);
	}

	private Boolean addExtension(IBrowserExtension extension) {
		for (Class<? extends IBrowserExtension> dependencyClass : extension
				.getDependencies()) {
			try {
				IBrowserExtension dependency = dependencyClass.newInstance();
				if (!this.addExtension(dependency)) {
					LOGGER.error("Dependency "
							+ dependency.getName()
							+ " could not be loaded. Still trying to add extension "
							+ extension.getName());
				}
			} catch (Exception e) {
				LOGGER.warn(
						"Cannot instantiate dependency "
								+ dependencyClass.getSimpleName()
								+ ". Skipping.", e);
			}
		}

		boolean success = true;
		for (File jsExtension : extension.getJsExtensions()) {
			try {
				this.runImmediately(jsExtension);
			} catch (Exception e) {
				LOGGER.error(
						"Could not load the JS extension \""
								+ extension.getName() + "\".", e);
				success = false;
			}
		}

		for (URI cssExtension : extension.getCssExtensions()) {
			this.injectCssFile(cssExtension);
		}

		return success;
	}

	private Boolean addExtensionOnce(IBrowserExtension extension)
			throws Exception {
		if (!this.hasExtension(extension)) {
			return this.addExtension(extension);
		}
		return true;
	}
}
