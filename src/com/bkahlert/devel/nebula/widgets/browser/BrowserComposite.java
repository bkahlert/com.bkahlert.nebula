package com.bkahlert.devel.nebula.widgets.browser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class BrowserComposite extends Composite implements
		IBrowserComposite {

	private static Logger LOGGER = Logger.getLogger(BrowserComposite.class);

	public static String getFileUrl(Class<?> clazz, String clazzRelativePath)
			throws IOException {
		URL timelineUrl = FileLocator.toFileURL(clazz
				.getResource(clazzRelativePath));
		String timelineUrlString = timelineUrl.toString().replace("file:",
				"file://");
		return timelineUrlString;
	}

	private Browser browser;
	private boolean completedLoading = false;
	private List<String> enqueuedJs = new ArrayList<String>();

	public BrowserComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.browser = new Browser(this, SWT.NONE);

		this.getBrowser().setUrl(getStartUrl());

		this.browser.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e);
			}
		});

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				completedLoading = true;
				for (Iterator<String> iterator = enqueuedJs.iterator(); iterator
						.hasNext();) {
					String js = iterator.next();
					iterator.remove();
					if (!BrowserComposite.this.browser.execute(js)) {
						LOGGER.error("Error occured while running JavaScript in browser: "
								+ js);
					}
				}
			}
		});
	}

	public abstract String getStartUrl();

	@Override
	public Browser getBrowser() {
		return this.browser;
	}

	@Override
	public boolean runJs(String js) {
		if (this.isDisposed())
			return false;
		boolean success = this.getBrowser().execute(js);
		if (!success) {
			LOGGER.error("Error occured while running JavaScript in browser: "
					+ js);
		}
		return success;
	}

	@Override
	public void enqueueJs(String js) {
		if (completedLoading) {
			runJs(js);
		} else {
			enqueuedJs.add(js);
		}
	}

	@Override
	public void injectCssFile(String path) {
		String js = "if(document.createStyleSheet){document.createStyleSheet(\""
				+ path
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ path + "\\\" type=\\\"text/css\\\" />\"))}";
		enqueueJs(js);
	}

}
