package com.bkahlert.nebula.widgets.browser.extended;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.ExecUtils;
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
		 * TODO FIX BUG: afterCompletion is called after the pageLoadScript. Two
		 * problems can occur: (1) pageLoadNeeds to access something loaded
		 * through extensions; (2) queued scripts get executed before extensions
		 * are loaded
		 */
		return ExecUtils.nonUIAsyncExec(ExtendedBrowser.class,
				"After Completion Extensions", new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (IBrowserExtension extension : ExtendedBrowser.this.extensions) {
							try {
								if (!extension.addExtensionOnce(
										ExtendedBrowser.this).get()) {
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
}
