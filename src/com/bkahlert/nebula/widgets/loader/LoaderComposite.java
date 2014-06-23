package com.bkahlert.nebula.widgets.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class LoaderComposite extends Composite {

	private static final Logger LOGGER = Logger
			.getLogger(LoaderComposite.class);

	public LoaderComposite(Composite parent) {
		super(parent, SWT.NONE);

		this.setLayout(new FillLayout());

		Browser browser = new Browser(this, SWT.NONE) {
			@Override
			public Future<Void> loading(boolean on) {
				return new CompletedFuture<Void>(null, null);
			}
		};
		browser.openBlank();
		try {
			browser.injectCssFile(new URI("file://"
					+ BrowserUtils.getFile(Loader.class, "spinner.css")));
		} catch (URISyntaxException e) {
			LOGGER.error(e);
		}
		browser.injectCss("html, body, div { width: 100%; height: 100%; padding: 0; margin: 0; }");
		browser.setBodyHtml("<div class=\"csspinner duo\"></div>");
	}

}
