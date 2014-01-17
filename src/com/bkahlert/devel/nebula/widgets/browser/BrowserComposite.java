package com.bkahlert.devel.nebula.widgets.browser;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
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
import com.bkahlert.devel.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.utils.CompletedFuture;

public class BrowserComposite extends Composite implements IBrowserComposite {

	private static Logger LOGGER = Logger.getLogger(BrowserComposite.class);

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath) {
		return getFileUrl(clazz, clazzRelativePath, "");
	}

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath,
			String suffix) {
		try {
			URL timelineUrl = FileLocator.toFileURL(clazz
					.getResource(clazzRelativePath));
			String timelineUrlString = timelineUrl.toString().replace("file:",
					"file://");
			return new URI(timelineUrlString + suffix);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Browser browser;
	private boolean settingUri = false;
	private boolean allowLocationChange = false;
	private boolean loadingCompleted = false;
	private List<IJavaScriptExceptionListener> javaScriptExceptionListeners = new ArrayList<IJavaScriptExceptionListener>();
	private List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();

	public BrowserComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.browser = new Browser(this, SWT.NONE);

		new BrowserFunction(this.getBrowser(), "error_callback") {
			@Override
			public Object function(Object[] arguments) {
				String filename = (String) arguments[0];
				Long lineNumber = Math.round((Double) arguments[1]);
				String detail = (String) arguments[2];

				JavaScriptException javaScriptException = new JavaScriptException(
						filename, lineNumber, detail);
				LOGGER.error(javaScriptException);
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

					String uri = BrowserComposite.this.browser.getUrl();
					final Future<Void> finished = BrowserComposite.this
							.afterCompletion(uri);
					ExecutorUtil.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								if (finished != null) {
									finished.get();
								}
							} catch (Exception e) {
								LOGGER.error(e);
							}

							// notify threads that want to run javascripts
							synchronized (BrowserComposite.this.monitor) {
								BrowserComposite.this.monitor.notifyAll();
							}
						}
					});
				}
			}
		});

		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				if (!BrowserComposite.this.settingUri) {
					IAnker anker = new Anker(event.location, null, null);
					for (IAnkerListener ankerListener : BrowserComposite.this.ankerListeners) {
						ankerListener.ankerClicked(anker);
					}
					event.doit = BrowserComposite.this.allowLocationChange;
				}
			}
		});
	}

	@Override
	public Future<Boolean> open(final String uri, final Integer timeout) {
		this.loadingCompleted = false;
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final AtomicReference<Boolean> isCancelled = new AtomicReference<Boolean>(
						false);

				// stops waiting after timeout
				Future<?> timeoutMonitor = null;
				if (timeout != null && timeout > 0) {
					timeoutMonitor = ExecutorUtil.nonUIAsyncRun(new Runnable() {
						@Override
						public void run() {
							synchronized (BrowserComposite.this.monitor) {
								if (!BrowserComposite.this.loadingCompleted) {
									isCancelled.set(true);
									BrowserComposite.this.monitor.notifyAll();
								}
							}
						}
					}, timeout);
				} else {
					LOGGER.warn("timeout must be greater or equal 0. Ignoring timeout.");
				}

				/*
				 * window["hoveredAnker"] = null;
				 * $("body").bind("DOMSubtreeModified" "beforeunload", function
				 * () { if (window["mouseleave"] && typeof window["mouseleave"])
				 * { window["mouseleave"](window["hoveredAnker"]) } });
				 * $("body").on({ mouseenter: function () { var e =
				 * $(this).clone().wrap(" <p> ").parent().html();
				 * window["hoveredAnker"] = e; if (window["mouseenter"] &&
				 * typeof window["mouseenter"]) { window["mouseenter"](e) } },
				 * mouseleave: function () { var e = $(this).clone().wrap(" <p>
				 * ").parent().html(); if (window["mouseleave"] && typeof
				 * window["mouseleave"]) { window["mouseleave"](e) } } }, "a")
				 */

				BrowserComposite.this.beforeLoad(uri);

				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						BrowserComposite.this.settingUri = true;
						BrowserComposite.this.browser.setUrl(uri.toString());
						BrowserComposite.this.activateExceptionHandling();
						BrowserComposite.this.settingUri = false;
					}
				});

				String js = "window[\"hoveredAnker\"]=null;$(\"body\").bind(\"DOMSubtreeModified beforeunload\",function(){if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](window[\"hoveredAnker\"])}});$(\"body\").on({mouseenter:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();window[\"hoveredAnker\"]=e;if(window[\"mouseenter\"]&&typeof window[\"mouseenter\"]){window[\"mouseenter\"](e)}},mouseleave:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](e)}}},\"a\")";
				BrowserComposite.this.run(js);

				BrowserComposite.this.afterLoad(uri);

				synchronized (BrowserComposite.this.monitor) {
					while (!BrowserComposite.this.loadingCompleted
							&& !isCancelled.get()) {
						LOGGER.debug("WAITING FOR LOADING COMPLETED:\nURI = "
								+ uri + "\nThread = " + Thread.currentThread()
								+ "\nLOADING COMPLETED = "
								+ BrowserComposite.this.loadingCompleted
								+ "\nTIMEOUT CANCELLED = " + isCancelled.get());
						BrowserComposite.this.monitor.wait();
						// notified by progresslistener or by timeout
					}

					if (timeoutMonitor != null) {
						timeoutMonitor.cancel(true);
					}

					return BrowserComposite.this.loadingCompleted;
				}
			}
		});
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout) {
		return this.open(uri.toString(), timeout);
	}

	@Override
	public Future<Boolean> openAboutBlank() {
		try {
			return this.open(new URI("about:blank"), 50);
		} catch (URISyntaxException e) {
			return new CompletedFuture<Boolean>(false, e);
		}
	}

	@Override
	public void setAllowLocationChange(boolean allow) {
		this.allowLocationChange = allow;
	}

	@Override
	public void beforeLoad(String uri) {
	}

	@Override
	public void afterLoad(String uri) {
	}

	@Override
	public Future<Void> afterCompletion(String uri) {
		return null;
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

	@Override
	public Future<Boolean> inject(URI script) {
		return this.run(script, false);
	}

	/**
	 * Notifies all registered {@link IJavaScriptExceptionListener}s in case a
	 * JavaScript error occurred.
	 */
	private void activateExceptionHandling() {
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
		try {
			this.run(new URI("file://" + script.getAbsolutePath()));
		} catch (URISyntaxException e) {
			LOGGER.error("Error running script included in " + script, e);
		}
	}

	@Override
	public Future<Boolean> run(final URI script) {
		return this.run(script, true);
	}

	private Future<Boolean> run(final URI script,
			final boolean removeAfterExecution) {
		Assert.isLegal(script != null);
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() {
				final String callbackFunctionName = new BigInteger(130,
						new SecureRandom()).toString(32);

				final Semaphore mutex = new Semaphore(0);
				ExecutorUtil.syncExec(new Runnable() {
					@Override
					public void run() {
						final AtomicReference<BrowserFunction> callback = new AtomicReference<BrowserFunction>();
						callback.set(new BrowserFunction(BrowserComposite.this
								.getBrowser(), callbackFunctionName) {
							@Override
							public Object function(Object[] arguments) {
								callback.get().dispose();
								mutex.release();
								return super.function(arguments);
							}
						});
					}
				});

				String js = "var h = document.getElementsByTagName(\"head\")[0]; var s = document.createElement(\"script\");s.type = \"text/javascript\";s.src = \""
						+ script.toString() + "\"; s.onload=function(e){";
				if (removeAfterExecution) {
					js += "h.removeChild(s);";
				}
				js += "window['" + callbackFunctionName + "']();";
				js += "};h.appendChild(s);";

				BrowserComposite.this.run(js);
				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					LOGGER.error(e);
				}
				return null;
			}
		});
	}

	@Override
	public <T> Future<T> run(final String script, final IConverter<T> converter) {
		Assert.isLegal(converter != null);
		if (this.getBrowser() == null || this.getBrowser().isDisposed()) {
			return null;
		}
		final Callable<T> callable = new Callable<T>() {
			@Override
			public T call() throws Exception {
				String logScript = script.length() > 100 ? script.substring(0,
						100) + "..." : script;
				logScript = logScript.replace("\n", " ").replace("\r", " ")
						.replace("\t", " ");
				LOGGER.info("Running " + logScript);
				Browser browser = BrowserComposite.this.getBrowser();
				if (browser == null || browser.isDisposed()) {
					return null;
				}
				Object returnValue = browser.evaluate(script);
				LOGGER.info("Returned " + returnValue);
				return converter.convert(returnValue);
			}
		};
		if (BrowserComposite.this.loadingCompleted) {
			final AtomicReference<T> converted = new AtomicReference<T>();
			Exception exception = null;
			try {
				converted.set(ExecutorUtil.syncExec(callable));
			} catch (Exception e) {
				exception = e;
			}
			return new CompletedFuture<T>(converted.get(), exception);
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
					// TODO possibly check if page was really loaded
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
	public Future<Object> run(IJavaScript script) {
		return this.run(script.toString());
	};

	@Override
	public void injectCssFile(URI uri) {
		this.run("if(document.createStyleSheet){document.createStyleSheet(\""
				+ uri.toString()
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ uri.toString() + "\\\" type=\\\"text/css\\\" />\"))}");
	}

	public void addJavaScriptExceptionListener(
			IJavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.add(javaScriptExceptionListener);
	}

	public void removeJavaScriptExceptionListener(
			IJavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.remove(javaScriptExceptionListener);
	}

	@Override
	public void addAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.add(ankerListener);
	}

	@Override
	public void removeAnkerListener(IAnkerListener ankerListener) {
		this.ankerListeners.remove(ankerListener);
	}

	@Override
	public Future<Boolean> containsElementWithID(String id) {
		return this.run("return document.getElementById('" + id + "') != null",
				IBrowserComposite.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Boolean> containsElementsWithName(String name) {
		return this.run("return document.getElementsByName('" + name
				+ "').length > 0", IBrowserComposite.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Void> setBodyHtml(String html) {
		String escapedHtml = html.replace("\n", "<br>").replace("&#xD;", "")
				.replace("\r", "").replace("\"", "\\\"").replace("'", "\\'");
		return this.run("document.body.innerHTML = ('" + escapedHtml + "');",
				IBrowserComposite.CONVERTER_VOID);
	}

	@Override
	public Future<String> getBodyHtml() {
		return this.run("return document.body.innerHTML",
				IBrowserComposite.CONVERTER_STRING);
	}

}
