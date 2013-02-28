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
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
	private boolean loadingCompleted = false;
	private boolean metaKeyPressed = false;
	private List<String> enqueuedJs = new ArrayList<String>();
	private List<IJavaScriptExceptionListener> javaScriptExceptionListeners = new ArrayList<IJavaScriptExceptionListener>();
	private List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();

	public BrowserComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.browser = new Browser(this, SWT.NONE);

		activateExceptionHandling();

		this.getBrowser().setUrl(getStartUrl());

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				loadingCompleted = true;
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

		Listener metaKeyListener = new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (e.type == SWT.FocusOut && e.widget != browser)
					return;

				metaKeyPressed = e.type == SWT.KeyDown
						&& ((e.stateMask & SWT.CTRL) != 0
								|| (e.stateMask & SWT.COMMAND) != 0
								|| (e.keyCode & SWT.CTRL) != 0 || (e.keyCode & SWT.COMMAND) != 0);
			}
		};
		Display.getCurrent().addFilter(SWT.KeyDown, metaKeyListener);
		Display.getCurrent().addFilter(SWT.KeyUp, metaKeyListener);
		Display.getCurrent().addFilter(SWT.FocusOut, metaKeyListener);

		this.browser.addLocationListener(new LocationAdapter() {
			public void changing(LocationEvent event) {
				IAnker anker = new Anker(event.location, null, null);
				if (metaKeyPressed) {
					for (IAnkerListener ankerListener : ankerListeners) {
						ankerListener.ankerClickedSpecial(anker);
					}
				} else {
					for (IAnkerListener ankerListener : ankerListeners) {
						ankerListener.ankerClicked(anker);
					}
				}
				event.doit = false;
			}
		});
	}

	/**
	 * Notifies all registered {@link IJavaScriptExceptionListener}s in case a
	 * JavaScript error occurred.
	 */
	private void activateExceptionHandling() {
		new BrowserFunction(this.getBrowser(), "error_callback") {
			@Override
			public Object function(Object[] arguments) {
				String filename = (String) arguments[0];
				Long lineNumber = Math.round((Double) arguments[1]);
				String detail = (String) arguments[2];

				JavaScriptException javaScriptException = new JavaScriptException(
						filename, lineNumber, detail);
				return fire(javaScriptException);
			}

			private boolean fire(JavaScriptException e) {
				boolean preventDefault = false;
				for (IJavaScriptExceptionListener javaScriptExceptionListener : javaScriptExceptionListeners) {
					if (javaScriptExceptionListener.thrown(e))
						preventDefault = true;
				}
				return preventDefault;
			}
		};

		this.getBrowser()
				.execute(
						"window.onerror = function(detail, filename, lineNumber) { if ( typeof window['error_callback'] !== 'function') return; return window['error_callback'](filename, lineNumber, detail); }");
	}

	/**
	 * Deactivate browser's native context/popup menu. Doing so allows the
	 * definition of menus in an inheriting composite via setMenu.
	 */
	public void deactivateNativeMenu() {
		this.getBrowser().addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	public abstract String getStartUrl();

	@Override
	public Browser getBrowser() {
		return this.browser;
	}

	public boolean isLoadingCompleted() {
		return loadingCompleted;
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
		if (loadingCompleted) {
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

	public void addJavaScriptExceptionListener(
			IJavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.add(javaScriptExceptionListener);
	}

	public void removeJavaScriptExceptionListener(
			IJavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.remove(javaScriptExceptionListener);
	}

	public void addAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.add(ankerListener);
	}

	public void removeAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.remove(ankerListener);
	}

}
