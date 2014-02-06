package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;

/**
 * This {@link IBrowserComposite} behaves like the {@link BrowserComposite} but
 * allows {@link IBrowserCompositeExtension}s to be automatically loaded when
 * the requested {@link URI} was loaded.
 * 
 * @author bkahlert
 * 
 */
public class ExtendedBrowserComposite extends BrowserComposite implements
		IBrowserComposite {

	private static final Logger LOGGER = Logger
			.getLogger(ExtendedBrowserComposite.class);

	private final IBrowserCompositeExtension[] extensions;

	public ExtendedBrowserComposite(Composite parent, int style,
			IBrowserCompositeExtension[] extensions) {
		super(parent, style);
		this.extensions = extensions;
	}

	@Override
	public Future<Void> afterCompletion(String uri) {
		return ExecUtils.nonUIAsyncExec(ExtendedBrowserComposite.class,
				"After Completion Extensions", new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (IBrowserCompositeExtension extension : ExtendedBrowserComposite.this.extensions) {
							try {
								extension.addExtensionOnce(
										ExtendedBrowserComposite.this).get();
							} catch (Exception e) {
								LOGGER.error(e);
							}
						}
						return null;
					}
				});
	}

}
