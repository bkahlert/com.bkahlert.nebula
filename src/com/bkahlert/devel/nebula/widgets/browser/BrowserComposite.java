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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bkahlert.devel.nebula.utils.EventDelegator;
import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.utils.OffWorker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.devel.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.browser.exception.BrowserTimeoutException;
import com.bkahlert.nebula.browser.exception.BrowserUninitializedException;
import com.bkahlert.nebula.browser.exception.ScriptExecutionException;
import com.bkahlert.nebula.utils.CompletedFuture;

public class BrowserComposite extends Composite implements IBrowserComposite {

	private static Logger LOGGER = Logger.getLogger(BrowserComposite.class);

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath) {
		return getFileUrl(clazz, clazzRelativePath, "");
	}

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath,
			String suffix) {
		try {
			URL url = FileLocator.toFileURL(clazz
					.getResource(clazzRelativePath));
			String timelineUrlString = url.toString().replace("file:",
					"file://");
			return new URI(timelineUrlString + suffix);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Browser browser;
	private boolean settingUri = false;
	private boolean allowLocationChange = false;
	private boolean firstLoadingStarted = false;
	private boolean loadingCompleted = false;
	final AtomicReference<Boolean> isCancelled = new AtomicReference<Boolean>(
			false);
	final AtomicReference<Exception> openException = new AtomicReference<Exception>(
			null);
	private String pageLoadCheckScript = null;
	private final List<IJavaScriptExceptionListener> javaScriptExceptionListeners = new ArrayList<IJavaScriptExceptionListener>();
	private final List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();

	private final OffWorker delayedScriptsWorker = new OffWorker(
			this.getClass(), "Script Runner");

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
				if (!BrowserComposite.this.loadingCompleted) {
					BrowserComposite.this.openException.set(e);
				}
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
				if (BrowserComposite.this.loadingCompleted
						|| BrowserComposite.this.isCancelled.get()
						|| BrowserComposite.this.browser == null
						|| BrowserComposite.this.browser.isDisposed()) {
					return;
				}

				try {
					/*
					 * WORKAROUND: If multiple browsers are instantiated it can
					 * occur that some have not loaded, yet. Therefore we poll
					 * the page until it is really loaded. Optionally a user
					 * provided pageLoadCheckScript is executed.
					 */
					String readyState = (String) BrowserComposite.this.browser
							.evaluate("return document.readyState;");
					if (readyState.equals("complete")
							&& (BrowserComposite.this.pageLoadCheckScript == null || IConverter.CONVERTER_BOOLEAN.convert(BrowserComposite.this.browser
									.evaluate(BrowserComposite.this.pageLoadCheckScript)))) {
						BrowserComposite.this.loadingCompleted = true;

						String uri = BrowserComposite.this.browser.getUrl();
						final Future<Void> finished = BrowserComposite.this
								.afterCompletion(uri);
						ExecUtils.nonUISyncExec(BrowserComposite.class,
								"Progress Check for " + uri, new Runnable() {
									@Override
									public void run() {
										try {
											if (finished != null) {
												finished.get();
											}
										} catch (Exception e) {
											LOGGER.error(e);
										}

										synchronized (BrowserComposite.this.monitor) {
											BrowserComposite.this.monitor
													.notifyAll();
										}
									}
								});
					} else {
						ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
								completed(null);
							}
						}, 50);
					}
				} catch (Exception e) {
					LOGGER.error(
							"An error occurred while checking the page load state",
							e);
					BrowserComposite.this.openException.set(e);
					synchronized (BrowserComposite.this.monitor) {
						BrowserComposite.this.monitor.notifyAll();
					}
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
					event.doit = BrowserComposite.this.allowLocationChange
							|| !BrowserComposite.this.loadingCompleted;
				}
			}

			// TODO call injectAnkerCode after a page has loaded a user clicked
			// on (or do all the same steps on first page load on all
			// consecutive loads)
		});

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (BrowserComposite.this.delayedScriptsWorker != null
						&& !BrowserComposite.this.delayedScriptsWorker
								.isShutdown()) {
					BrowserComposite.this.delayedScriptsWorker.shutdown();
				}
			}
		});
	}

	private boolean successfullyInjectedAnkerHoverCallback = false;

	/**
	 * Injects the code needed for {@link #addAnkerListener(IAnkerListener)} and
	 * {@link #removeAnkerListener(IAnkerListener)} to work.
	 * <p>
	 * The JavaScript remembers a successful injection in case to consecutive
	 * calls are made.
	 * <p>
	 * As soon as a successful injection has been registered,
	 * {@link #successfullyInjectedAnkerHoverCallback} is set so no unnecessary
	 * further injection is made.
	 * <p>
	 * This method should be called after every attempt to register an
	 * {@link IAnkerListener}.
	 */
	private void injectAnkerHoverCallback() {
		if (this.successfullyInjectedAnkerHoverCallback) {
			return;
		}

		String js = "return(function($){if(!$ || window[\"successfullyInjectedAnkerHoverCallback\"])return false;window[\"hoveredAnker\"]=null;$(\"body\").bind(\"DOMSubtreeModified beforeunload\",function(){if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](window[\"hoveredAnker\"])}});$(\"body\").on({mouseenter:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();window[\"hoveredAnker\"]=e;if(window[\"mouseenter\"]&&typeof window[\"mouseenter\"]){window[\"mouseenter\"](e)}},mouseleave:function(){var e=$(this).clone().wrap(\"<p>\").parent().html();if(window[\"mouseleave\"]&&typeof window[\"mouseleave\"]){window[\"mouseleave\"](e)}}},\"a\");window[\"successfullyInjectedAnkerHoverCallback\"]=true;return true;})(typeof(jQuery)!=='undefined'?jQuery:null)";
		final Future<Boolean> success = BrowserComposite.this.run(js,
				IConverter.CONVERTER_BOOLEAN);
		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					if (success.get()) {
						BrowserComposite.this.successfullyInjectedAnkerHoverCallback = true;
					}
				} catch (Exception e) {
					LOGGER.error(
							"Could not inject anker hover callback code in "
									+ BrowserComposite.this.getClass()
											.getSimpleName(), e);
				}
			}
		});
	}

	@Override
	public Future<Boolean> open(String address, Integer timeout) {
		return this.open(address, timeout, null);
	}

	@Override
	public Future<Boolean> open(final String uri, final Integer timeout,
			String pageLoadCheckScript) {
		this.firstLoadingStarted = true;
		this.loadingCompleted = false;
		this.pageLoadCheckScript = pageLoadCheckScript;
		this.successfullyInjectedAnkerHoverCallback = false;
		this.browser.setUrl(uri.toString());

		this.injectAnkerHoverCallback();

		return ExecUtils.nonUIAsyncExec(BrowserComposite.class, "Opening "
				+ uri, new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				BrowserComposite.this.isCancelled.set(false);
				BrowserComposite.this.openException.set(null);

				// stops waiting after timeout
				Future<?> timeoutMonitor = null;
				if (timeout != null && timeout > 0) {
					timeoutMonitor = ExecUtils.nonUIAsyncExec(
							BrowserComposite.class, "Timeout Watcher for "
									+ uri, new Runnable() {
								@Override
								public void run() {
									synchronized (BrowserComposite.this.monitor) {
										if (!BrowserComposite.this.loadingCompleted) {
											BrowserComposite.this.isCancelled
													.set(true);
											BrowserComposite.this.monitor
													.notifyAll();
										}
									}
								}
							}, timeout);
				} else {
					LOGGER.warn("timeout must be greater or equal 0. Ignoring timeout.");
				}

				BrowserComposite.this.beforeLoad(uri);

				ExecUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						BrowserComposite.this.settingUri = true;
						BrowserComposite.this.browser.setUrl(uri.toString());
						BrowserComposite.this.activateExceptionHandling();
						BrowserComposite.this.settingUri = false;
					}
				});

				BrowserComposite.this.afterLoad(uri);

				synchronized (BrowserComposite.this.monitor) {
					while (!BrowserComposite.this.loadingCompleted
							&& !BrowserComposite.this.isCancelled.get()) {
						LOGGER.debug("Waiting for " + uri
								+ " to be loaded (Thread: "
								+ Thread.currentThread() + "; completed: "
								+ BrowserComposite.this.loadingCompleted
								+ "; timed out: "
								+ BrowserComposite.this.isCancelled.get() + ")");
						BrowserComposite.this.monitor.wait();
						// notified by progresslistener or by timeout
					}

					if (BrowserComposite.this.openException.get() != null) {
						throw BrowserComposite.this.openException.get();
					}

					if (timeoutMonitor != null) {
						timeoutMonitor.cancel(true);
					}

					if (BrowserComposite.this.loadingCompleted == BrowserComposite.this.isCancelled
							.get()) {
						throw new RuntimeException("Implementation error");
					}

					if (BrowserComposite.this.loadingCompleted) {
						LOGGER.debug("Successfully loaded " + uri);
						BrowserComposite.this.delayedScriptsWorker.start();
					} else {
						LOGGER.error("Aborted loading " + uri
								+ " due to timeout");
					}

					BrowserComposite.this.delayedScriptsWorker.finish();

					return BrowserComposite.this.loadingCompleted;
				}
			}
		});
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout) {
		return this.open(uri.toString(), timeout, null);
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout,
			String pageLoadCheckScript) {
		return this.open(uri.toString(), timeout, pageLoadCheckScript);
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

	private final Object monitor = new Object();

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
		return ExecUtils.nonUIAsyncExec(BrowserComposite.class,
				"Script Runner for: " + script, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						final String callbackFunctionName = "_"
								+ new BigInteger(130, new SecureRandom())
										.toString(32);

						final Semaphore mutex = new Semaphore(0);
						ExecUtils.syncExec(new Runnable() {
							@Override
							public void run() {
								final AtomicReference<BrowserFunction> callback = new AtomicReference<BrowserFunction>();
								callback.set(new BrowserFunction(
										BrowserComposite.this.getBrowser(),
										callbackFunctionName) {
									@Override
									public Object function(Object[] arguments) {
										callback.get().dispose();
										mutex.release();
										return null;
									}
								});
							}
						});

						String js = "var h = document.getElementsByTagName(\"head\")[0]; var s = document.createElement(\"script\");s.type = \"text/javascript\";s.src = \""
								+ script.toString()
								+ "\"; s.onload=function(e){";
						if (removeAfterExecution) {
							js += "h.removeChild(s);";
						}
						js += callbackFunctionName + "();";
						js += "};h.appendChild(s);";

						// runs the scripts that ends by calling the callback
						// ...
						BrowserComposite.this.run(js);
						try {
							// ... which destroys itself and releases this lock
							mutex.acquire();
						} catch (InterruptedException e) {
							LOGGER.error(e);
						}
						return null;
					}
				});
	}

	@Override
	public <DEST> Future<DEST> run(final String script,
			final IConverter<Object, DEST> converter) {
		Assert.isLegal(converter != null);
		if (this.getBrowser() == null || this.getBrowser().isDisposed()) {
			return null;
		}
		this.scriptEnqueued(script);
		final String[] logScript = new String[] { script.length() > 100 ? script
				.substring(0, 100) + "..."
				: script };
		logScript[0] = logScript[0].replace("\n", " ").replace("\r", " ")
				.replace("\t", " ");
		final Callable<DEST> callable = ExecUtils.createThreadLabelingCode(
				new Callable<DEST>() {
					@Override
					public DEST call() throws Exception {
						LOGGER.info("Running " + logScript[0]);
						final Browser browser = BrowserComposite.this
								.getBrowser();
						try {
							BrowserComposite.this
									.scriptAboutToBeSentToBrowser(script);
							Object returnValue = browser.evaluate(script);
							BrowserComposite.this
									.scriptReturnValueReceived(returnValue);
							LOGGER.info("Returned " + returnValue);
							return converter.convert(returnValue);
						} catch (Exception e) {
							LOGGER.error(e);
							throw e;
						}
					}
				}, BrowserComposite.class, "Running " + logScript[0]);
		if (BrowserComposite.this.loadingCompleted) {
			final AtomicReference<DEST> converted = new AtomicReference<DEST>();
			Exception exception = null;
			try {
				converted.set(ExecUtils.syncExec(callable));
			} catch (Exception e) {
				exception = e;
			}
			return new CompletedFuture<DEST>(converted.get(), exception);
		} else if (this.isCancelled.get()) {
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(new JavaScript(script),
							new BrowserTimeoutException()));
		} else if (!this.firstLoadingStarted) {
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(new JavaScript(script),
							new BrowserUninitializedException(this)));
		} else {
			return this.delayedScriptsWorker.submit(new Callable<DEST>() {
				@Override
				public DEST call() throws Exception {
					if (BrowserComposite.this.isDisposed()) {
						return null;
					}
					if (BrowserComposite.this.loadingCompleted) {
						return ExecUtils.syncExec(callable);
					} else {
						throw new ScriptExecutionException(new JavaScript(
								script), new BrowserTimeoutException());
					}
				}
			});
		}
	}

	@Override
	public Future<Object> run(final String script) {
		return this.run(script, new IConverter<Object, Object>() {
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

	/**
	 * Testers can override this method to see what goint to be executed.
	 * 
	 * @param script
	 */
	public void scriptEnqueued(String script) {
	}

	/**
	 * Testers can override this method to see what's executed.
	 * 
	 * @param script
	 */
	public void scriptAboutToBeSentToBrowser(String script) {
	}

	/**
	 * Testers can override this method to chec the script execution results.
	 * 
	 * @param returnValue
	 */
	public void scriptReturnValueReceived(Object returnValue) {
	}

	@Override
	public void injectCssFile(URI uri) {
		this.run("if(document.createStyleSheet){document.createStyleSheet(\""
				+ uri.toString()
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ uri.toString() + "\\\" type=\\\"text/css\\\" />\"))}");
	}

	@Override
	public void injectCss(String css) {
		String script = "(function(){var style=document.createElement(\"style\");style.appendChild(document.createTextNode(\""
				+ css
				+ "\"));(document.getElementsByTagName(\"head\")[0]||document.documentElement).appendChild(style)})()";
		this.run(script);
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
				IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Boolean> containsElementsWithName(String name) {
		return this.run("return document.getElementsByName('" + name
				+ "').length > 0", IConverter.CONVERTER_BOOLEAN);
	}

	@Override
	public Future<Void> setBodyHtml(String html) {
		String escapedHtml = html.replace("\n", "<br>").replace("&#xD;", "")
				.replace("\r", "").replace("\"", "\\\"").replace("'", "\\'");
		return this.run("document.body.innerHTML = ('" + escapedHtml + "');",
				IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<String> getBodyHtml() {
		return this.run("return document.body.innerHTML",
				IConverter.CONVERTER_STRING);
	}

}
