package com.bkahlert.nebula.handlers;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.Browser;

public class BrowserShowSourceHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(BrowserShowSourceHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Control control = Display.getCurrent().getCursorControl();
		if (!(control instanceof org.eclipse.swt.browser.Browser)) {
			control = Display.getCurrent().getFocusControl();
		}
		if (control instanceof org.eclipse.swt.browser.Browser) {
			org.eclipse.swt.browser.Browser swtBrowser = (org.eclipse.swt.browser.Browser) control;
			if (swtBrowser.getParent() instanceof Browser) {
				final Browser browser = (Browser) swtBrowser.getParent();
				try {
					ExecUtils.nonUIAsyncExec(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							try {
								String html = browser.getHtml().get();
								File tmp = File.createTempFile("Browser",
										".html");
								tmp.deleteOnExit();
								FileUtils.write(tmp, html);
								IWebBrowser browser = PlatformUI.getWorkbench()
										.getBrowserSupport()
										.getExternalBrowser();
								browser.openURL(new URL("file://"
										+ tmp.getAbsolutePath()));
							} catch (Exception e) {
								LOGGER.error(e);
							}
							return null;
						}
					});
				} catch (Exception e) {
					LOGGER.error(e);
					return null;
				}
			}
		}
		return null;
	}
}
