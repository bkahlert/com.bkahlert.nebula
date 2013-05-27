package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bkahlert.devel.nebula.utils.EventDelegator;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.utils.CompletedFuture;

public abstract class BrowserComposite extends Composite implements
		IBrowserComposite {

	private static Logger LOGGER = Logger.getLogger(BrowserComposite.class);

	public static String getFileUrl(Class<?> clazz, String clazzRelativePath) {
		try {
			URL timelineUrl = FileLocator.toFileURL(clazz
					.getResource(clazzRelativePath));
			String timelineUrlString = timelineUrl.toString().replace("file:",
					"file://");
			return timelineUrlString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Browser browser;
	private boolean loadingCompleted = false;
	private List<IJavaScriptExceptionListener> javaScriptExceptionListeners = new ArrayList<IJavaScriptExceptionListener>();
	private List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();

	public BrowserComposite(Composite parent, int style, String url) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.browser = new Browser(this, SWT.NONE);

		this.activateExceptionHandling();

		this.run("$(\"body\").on({mouseenter:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();if(window[\"mouseenter\"]&&typeof window[\"mouseenter\"]){window[\"mouseenter\"](e)}},mouseleave:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](e)}}},\"a\")");
		new BrowserFunction(this.browser, "mouseenter") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					BrowserComposite.this.fireAnkerHover((String) arguments[0],
							true);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "mouseleave") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					BrowserComposite.this.fireAnkerHover((String) arguments[0],
							false);
				}
				return null;
			}
		};

		this.getBrowser().setUrl(url);

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				// WORKAROUND: If multiple browsers are instantiated it can
				// occur
				// that some have not loaded, yet! Therefore we poll the page
				// until it is really loaded.
				String readyState = (String) BrowserComposite.this.browser
						.evaluate("return document.readyState;");
				if (!readyState.equals("complete")) {
					ExecutorUtil.asyncExec(new Runnable() {
						@Override
						public void run() {
							completed(null);
						}
					}, 50);
				} else {
					BrowserComposite.this.loadingCompleted = true;
					synchronized (BrowserComposite.this.monitor) {
						BrowserComposite.this.monitor.notifyAll();
					}
				}
			}
		});

		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				IAnker anker = new Anker(event.location, null, null);
				for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
					ankerListener.ankerClicked(anker);
				}
				event.doit = false;
			}
		});
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		// TODO evtl. erst ausführen, wenn alles wirklich geladen wurde, um
		// evtl. falsche Mauskoordinaten zu verhindern und so ein Fehlverhalten
		// im InformationControl vorzeugen
		if (EventDelegator.mustDelegate(eventType, this)) {
			this.browser.addListener(eventType, listener);
		} else {
			super.addListener(eventType, listener);
		}
	}

	/**
	 * 
	 * @param string
	 * @param mouseEnter
	 *            true if mouseenter; false otherwise
	 */
	protected void fireAnkerHover(String html, boolean mouseEnter) {
		Document document = Jsoup.parse(html);
		Elements elements = document.getElementsByTag("a");
		for (Element element : elements) {
			String href = element.attr("href");
			if (href == null) {
				href = element.attr("data-cke-saved-href");
			}
			String[] classes = element.attr("class") != null ? element.attr(
					"class").split("\\s+") : new String[0];
			String content = element.text();

			IAnker anker = new Anker(href, classes, content);
			for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
				ankerListener.ankerHovered(anker, mouseEnter);
			}
		}
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
				return this.fire(javaScriptException);
			}

			private boolean fire(JavaScriptException e) {
				boolean preventDefault = false;
				for (IJavaScriptExceptionListener javaScriptExceptionListener : BrowserComposite.this.javaScriptExceptionListeners) {
					if (javaScriptExceptionListener.thrown(e)) {
						preventDefault = true;
					}
				}
				return preventDefault;
			}
		};

		this.getBrowser()
				.execute(
						"window.onerror = function(detail, filename, lineNumber) { if ( typeof window['error_callback'] !== 'function') return; return window['error_callback'](filename ? filename : 'unknown file', lineNumber ? lineNumber : 'unknown line number', detail ? detail : 'unknown detail'); }");
	}

	/**
	 * Deactivate browser's native context/popup menu. Doing so allows the
	 * definition of menus in an inheriting composite via setMenu.
	 */
	public void deactivateNativeMenu() {
		this.getBrowser().addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	@Override
	public Browser getBrowser() {
		return this.browser;
	}

	public boolean isLoadingCompleted() {
		return this.loadingCompleted;
	}

	private Object monitor = new Object();

	@Override
	public void run(final File script) {
		Assert.isLegal(script.canRead());
		ExecutorUtil.nonUIExec(new Runnable() {
			@Override
			public void run() {
				String js = "var h = document.getElementsByTagName(\"head\")[0];var s = document.createElement(\"script\");s.type = \"text/javascript\";s.src = \"file://"
						+ script.getAbsolutePath()
						+ "\";s.onload=function(e){h.removeChild(s);};h.appendChild(s);";
				BrowserComposite.this.run(js);
			}
		});
	}

	@Override
	public <T> Future<T> run(final String script, final IConverter<T> converter) {
		Assert.isLegal(converter != null);
		if (this.getBrowser() == null || this.getBrowser().isDisposed()) {
			return null;
		}
		// System.out.println(script);
		final Callable<T> callable = new Callable<T>() {
			@Override
			public T call() throws Exception {
				Browser browser = BrowserComposite.this.getBrowser();
				if (browser == null || browser.isDisposed()) {
					return null;
				}
				Object returnValue = browser.evaluate(script);
				return converter.convert(returnValue);
			}
		};
		if (BrowserComposite.this.loadingCompleted) {
			final AtomicReference<T> converted = new AtomicReference<T>();
			try {
				converted.set(ExecutorUtil.syncExec(callable));
			} catch (Exception e) {
				LOGGER.fatal(e);
			}
			return new CompletedFuture<T>(converted.get());
		} else {
			return ExecutorUtil.nonUIAsyncExec(new Callable<T>() {
				@Override
				public T call() throws Exception {
					if (!BrowserComposite.this.loadingCompleted) {
						synchronized (BrowserComposite.this.monitor) {
							BrowserComposite.this.monitor.wait();
						}
					}
					if (BrowserComposite.this.isDisposed()) {
						return null;
					}
					return ExecutorUtil.syncExec(callable);
				}
			});
		}
	}

	@Override
	public Future<Object> run(final String script) {
		return this.run(script, new IConverter<Object>() {
			@Override
			public Object convert(Object object) {
				return object;
			}
		});
	}

	@Override
	public void injectCssFile(String path) {
		this.run("if(document.createStyleSheet){document.createStyleSheet(\""
				+ path
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ path + "\\\" type=\\\"text/css\\\" />\"))}");
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
