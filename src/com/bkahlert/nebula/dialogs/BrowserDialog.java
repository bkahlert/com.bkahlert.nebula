package com.bkahlert.nebula.dialogs;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.JQueryEnabledBrowserComposite;
import com.bkahlert.nebula.utils.HttpUtils;

public class BrowserDialog extends Dialog {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(BrowserDialog.class);

	private JQueryEnabledBrowserComposite browser;

	public BrowserDialog(Shell parentShell) {
		super(parentShell);
	}

	private void resize(Point windowDimensions) {
		Rectangle trim = this.getShell().computeTrim(0, 0, windowDimensions.x,
				windowDimensions.y);
		this.getShell().setSize(trim.width, trim.height);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new FillLayout());

		this.browser = new JQueryEnabledBrowserComposite(composite, SWT.NONE);

		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		return parent;
	}

	public <T> Future<T> open(final String url, final Point windowDimensions,
			final Point scrollPosition, final int timeout,
			final Callable<T> callable) throws BrowserException {
		return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final URL url_ = url != null ? new URL(url) : null;

				if (url_ != null) {
					LOGGER.info("Opening new page: " + url_.toString());
				} else {
					LOGGER.info("Working on already opened page");
				}
				if (url_ != null) {
					int responseCode = HttpUtils.getResponseCode(url_);
					if (responseCode < 200 || responseCode >= 300) {
						throw new BrowserException("Unsupported response code "
								+ responseCode + " returned for " + url);
					}
				}

				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						BrowserDialog.this.setBlockOnOpen(false);
						BrowserDialog.this.open();
						BrowserDialog.this.resize(windowDimensions);
					}
				});

				if (url_ != null) {
					if (BrowserDialog.this.browser.open(url_.toURI(), timeout)
							.get()) {
						boolean scrolled = BrowserDialog.this.browser.scrollTo(
								scrollPosition.x, scrollPosition.y).get();
						if (scrolled) {
							Thread.sleep(800);
						}
						if (callable != null) {
							return callable.call();
						} else {
							return null;
						}
					} else {
						throw new BrowserException("Opening " + url_.toURI()
								+ " timeout out");
					}
				} else {
					try {
						BrowserDialog.this.browser.scrollTo(scrollPosition.x,
								scrollPosition.y).get();
						if (callable != null) {
							return callable.call();
						} else {
							return null;
						}
					} catch (Exception e) {
						throw new BrowserException(e);
					}
				}
			}
		});
	}

	public JQueryEnabledBrowserComposite getBrowser() {
		return this.browser;
	}

}
