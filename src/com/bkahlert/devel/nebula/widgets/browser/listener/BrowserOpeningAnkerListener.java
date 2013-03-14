package com.bkahlert.devel.nebula.widgets.browser.listener;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public class BrowserOpeningAnkerListener extends AnkerAdaptingListener {

	private static final Logger LOGGER = Logger
			.getLogger(BrowserOpeningAnkerListener.class);

	public BrowserOpeningAnkerListener() {
		super(new IURIListener() {
			@Override
			public void uriClicked(URI uri) {
				try {
					IWebBrowser browser = PlatformUI.getWorkbench()
							.getBrowserSupport().getExternalBrowser();
					browser.openURL(new URL(uri.toString()));
				} catch (PartInitException e) {
					LOGGER.error(
							"Can't open external browser to open "
									+ uri.toString(), e);
				} catch (MalformedURLException e) {
					LOGGER.error("Can't convert " + URI.class.getSimpleName()
							+ " to " + URL.class.getSimpleName() + ": "
							+ uri.toString());
				}
			}

			@Override
			public void uriHovered(URI uri, boolean entered) {
				return;
			}
		});
	}

}
