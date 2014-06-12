package com.bkahlert.nebula.widgets.browser;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.swt.IFocusService;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.EventDelegator;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.widgets.browser.extended.html.Element;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IDropListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner.BrowserStatus;

public class Browser extends Composite implements IBrowser {

	private static IFocusService FOCUS_SERVICE = null;
	private static IContextService CONTEXT_SERVICE = null;

	static {
		try {
			FOCUS_SERVICE = (IFocusService) PlatformUI.getWorkbench()
					.getService(IFocusService.class);
			CONTEXT_SERVICE = (IContextService) PlatformUI.getWorkbench()
					.getService(IContextService.class);
		} catch (NoClassDefFoundError e) {

		}
	}

	private static Logger LOGGER = Logger.getLogger(Browser.class);
	public static final String FOCUS_ID = "com.bkahlert.nebula.browser";

	private org.eclipse.swt.browser.Browser browser;
	private BrowserScriptRunner browserScriptRunner;

	private boolean settingUri = false;
	private boolean allowLocationChange = false;
	private Rectangle cachedContentBounds = null;

	private final List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();
	private final List<IMouseListener> mouseListeners = new ArrayList<IMouseListener>();
	private final List<IFocusListener> focusListeners = new ArrayList<IFocusListener>();
	private final List<IDropListener> dropListeners = new ArrayList<IDropListener>();

	public Browser(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.browser = new org.eclipse.swt.browser.Browser(this, SWT.NONE);
		this.browserScriptRunner = new BrowserScriptRunner(this.browser) {
			@Override
			public void scriptAboutToBeSentToBrowser(String script) {
				Browser.this.scriptAboutToBeSentToBrowser(script);
			}

			@Override
			public void scriptReturnValueReceived(Object returnValue) {
				Browser.this.scriptReturnValueReceived(returnValue);
			}
		};

		new BrowserFunction(this.browser, "__mouseenter") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					Browser.this.fireAnkerHover((String) arguments[0], true);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__mouseleave") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					Browser.this.fireAnkerHover((String) arguments[0], false);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__mousemove") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 2
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)) {
					Browser.this.fireMouseMove((Double) arguments[0],
							(Double) arguments[1]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__mousedown") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 2
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)) {
					Browser.this.fireMouseDown((Double) arguments[0],
							(Double) arguments[1]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__mouseup") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 2
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)) {
					Browser.this.fireMouseUp((Double) arguments[0],
							(Double) arguments[1]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__click") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					Browser.this.fireAnkerClicked((String) arguments[0]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__focusgained") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					final IElement element = new Element((String) arguments[0]);
					Browser.this.fireFocusGained(element);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__focuslost") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1 && arguments[0] instanceof String) {
					final IElement element = new Element((String) arguments[0]);
					Browser.this.fireFocusLost(element);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__resize") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 4
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)
						&& (arguments[2] == null || arguments[2] instanceof Double)
						&& (arguments[3] == null || arguments[3] instanceof Double)) {
					Browser.this.cachedContentBounds = new Rectangle(
							arguments[0] != null ? (int) Math.round((Double) arguments[0])
									: Integer.MAX_VALUE,
							arguments[1] != null ? (int) Math
									.round((Double) arguments[1])
									: Integer.MAX_VALUE,
							arguments[2] != null ? (int) Math
									.round((Double) arguments[2])
									: Integer.MAX_VALUE,
							arguments[3] != null ? (int) Math
									.round((Double) arguments[3])
									: Integer.MAX_VALUE);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__drop") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 3 && arguments[0] instanceof Double
						&& arguments[1] instanceof Double
						&& arguments[2] instanceof String) {
					long offsetX = Math.round((Double) arguments[0]);
					long offsetY = Math.round((Double) arguments[1]);
					String data = (String) arguments[2];
					Browser.this.fireDrop(offsetX, offsetY, data);
				}
				return null;
			}
		};

		// needed so paste action can be overwritten
		// @see
		// http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fui%2Fswt%2FIFocusService.html
		if (FOCUS_SERVICE != null) {
			FOCUS_SERVICE.addFocusTracker(this.browser, FOCUS_ID);
		}
		this.browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (FOCUS_SERVICE != null) {
					FOCUS_SERVICE.removeFocusTracker(Browser.this.browser);
				}
			}
		});

		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				if (!Browser.this.settingUri) {
					event.doit = Browser.this.allowLocationChange
							|| Browser.this.browserScriptRunner
									.getBrowserStatus() == BrowserStatus.LOADING;
				}
			}

			// TODO call injectAnkerCode after a page has loaded a user clicked
			// on (or do all the same steps on first page load on all
			// consecutive loads)
		});

		this.browser.addFocusListener(new FocusListener() {
			private IContextActivation activation = null;

			@Override
			public void focusGained(FocusEvent e) {
				if (CONTEXT_SERVICE != null) {
					this.activation = CONTEXT_SERVICE
							.activateContext("com.bkahlert.ui.browser");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (CONTEXT_SERVICE != null) {
					CONTEXT_SERVICE.deactivateContext(this.activation);
				}
			}
		});

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				synchronized (Browser.this.monitor) {
					if (Browser.this.browserScriptRunner.getBrowserStatus() == BrowserStatus.LOADING) {
						Browser.this.browserScriptRunner
								.setBrowserStatus(BrowserStatus.CANCELLED);
					}
					Browser.this.browserScriptRunner.dispose();
					Browser.this.monitor.notifyAll();
				}
			}
		});
	}

	private boolean eventCatchScriptInjected = false;

	/**
	 * Injects the code needed for {@link #addAnkerListener(IAnkerListener)},
	 * {@link #addFokusListener(IFocusListener)} and
	 * {@link #addDropListener(IFocusListener)} to work.
	 * <p>
	 * The JavaScript remembers a successful injection in case to consecutive
	 * calls are made.
	 * <p>
	 * As soon as a successful injection has been registered,
	 * {@link #eventCatchScriptInjected} is set so no unnecessary further
	 * injection is made.
	 */
	private void injectEventCatchScript() {
		if (this.eventCatchScriptInjected) {
			return;
		}

		File events = BrowserUtils.getFile(Browser.class, "events.js");
		try {
			Browser.this.runImmediately(events);
		} catch (Exception e) {
			LOGGER.error("Could not inject events catch script in "
					+ Browser.this.getClass().getSimpleName(), e);
		}

		File dnd = BrowserUtils.getFile(Browser.class, "dnd.js");
		try {
			Browser.this.runImmediately(dnd);
		} catch (Exception e) {
			LOGGER.error("Could not inject drop catch script in "
					+ Browser.this.getClass().getSimpleName(), e);
		}

		Browser.this.eventCatchScriptInjected = true;
	}

	@Override
	public Future<Boolean> open(String address, Integer timeout) {
		return this.open(address, timeout, null);
	}

	@Override
	public Future<Boolean> open(final String uri, final Integer timeout,
			final String pageLoadCheckScript) {
		if (this.browser.isDisposed()) {
			throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
		}

		Browser.this.browserScriptRunner
				.setBrowserStatus(BrowserStatus.LOADING);

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				if (Browser.this.browser == null
						|| Browser.this.browser.isDisposed()) {
					return;
				}

				if (Browser.this.browserScriptRunner.getBrowserStatus() != BrowserStatus.LOADING) {
					if (Browser.this.browserScriptRunner.getBrowserStatus() != BrowserStatus.CANCELLED) {
						LOGGER.error("State Error: "
								+ Browser.this.browserScriptRunner
										.getBrowserStatus());
					}
					return;
				}

				try {
					/*
					 * WORKAROUND: If multiple browsers are instantiated it can
					 * occur that some have not loaded, yet. Therefore we poll
					 * the page until it is really loaded. Optionally a user
					 * provided pageLoadCheckScript is executed.
					 */
					String readyState = Browser.this.browserScriptRunner
							.runImmediately("return document.readyState;",
									IConverter.CONVERTER_STRING);
					if (readyState.equals("complete")
							&& (pageLoadCheckScript == null || Browser.this.browserScriptRunner
									.runImmediately(pageLoadCheckScript,
											IConverter.CONVERTER_BOOLEAN))) {

						final String uri = Browser.this.browser.getUrl();
						final Future<Void> finished = Browser.this
								.beforeCompletion(uri);
						ExecUtils.nonUISyncExec(Browser.class,
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

										Browser.this.injectEventCatchScript();
										synchronized (Browser.this.monitor) {
											if (Browser.this.browserScriptRunner
													.getBrowserStatus() != BrowserStatus.CANCELLED) {
												Browser.this.browserScriptRunner
														.setBrowserStatus(BrowserStatus.LOADED);
											}
											Browser.this.monitor.notifyAll();
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
					synchronized (Browser.this.monitor) {
						Browser.this.monitor.notifyAll();
					}
				}
			}
		});

		return ExecUtils.nonUIAsyncExec(Browser.class, "Opening " + uri,
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						// stops waiting after timeout
						Future<Void> timeoutMonitor = null;
						if (timeout != null && timeout > 0) {
							timeoutMonitor = ExecUtils.nonUIAsyncExec(
									Browser.class,
									"Timeout Watcher for " + uri,
									new Runnable() {
										@Override
										public void run() {
											synchronized (Browser.this.monitor) {
												if (Browser.this.browserScriptRunner
														.getBrowserStatus() != BrowserStatus.LOADED) {
													Browser.this.browserScriptRunner
															.setBrowserStatus(BrowserStatus.CANCELLED);
												}
												Browser.this.monitor
														.notifyAll();
											}
										}
									}, timeout);
						} else {
							LOGGER.warn("timeout must be greater or equal 0. Ignoring timeout.");
						}

						Browser.this.beforeLoad(uri);

						ExecUtils.syncExec(new Runnable() {
							@Override
							public void run() {
								Browser.this.settingUri = true;
								Browser.this.browser.setUrl(uri.toString());
								Browser.this.settingUri = false;
							}
						});

						Browser.this.afterLoad(uri);

						synchronized (Browser.this.monitor) {
							if (Browser.this.browserScriptRunner
									.getBrowserStatus() == BrowserStatus.LOADING) {
								LOGGER.debug("Waiting for "
										+ uri
										+ " to be loaded (Thread: "
										+ Thread.currentThread()
										+ "; status: "
										+ Browser.this.browserScriptRunner
												.getBrowserStatus() + ")");
								Browser.this.monitor.wait();
								// notified by progresslistener or by timeout
							}

							if (timeoutMonitor != null) {
								timeoutMonitor.cancel(true);
							}

							switch (Browser.this.browserScriptRunner
									.getBrowserStatus()) {
							case LOADED:
								LOGGER.debug("Successfully loaded " + uri);
								break;
							case CANCELLED:
								LOGGER.warn("Aborted loading " + uri
										+ " due to timeout");
								break;
							default:
								throw new RuntimeException(
										"Implementation error");
							}

							return Browser.this.browserScriptRunner
									.getBrowserStatus() == BrowserStatus.LOADED;
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
	public Future<Boolean> openBlank() {
		try {
			File empty = File.createTempFile("blank", ".html");
			FileUtils.writeStringToFile(empty,
					"<html><head></head><body></body></html>", "UTF-8");
			return this.open(new URI("file://" + empty.getAbsolutePath()),
					60000);
		} catch (Exception e) {
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
	public Future<Void> beforeCompletion(String uri) {
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
	 * Deactivate browser's native context/popup menu. Doing so allows the
	 * definition of menus in an inheriting composite via setMenu.
	 */
	public void deactivateNativeMenu() {
		this.browser.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	@Override
	public org.eclipse.swt.browser.Browser getBrowser() {
		return this.browser;
	}

	public boolean isLoadingCompleted() {
		return this.browserScriptRunner.getBrowserStatus() == BrowserStatus.LOADED;
	}

	private final Object monitor = new Object();

	@Override
	public Future<Boolean> inject(URI script) {
		return this.browserScriptRunner.inject(script);
	}

	@Override
	public Future<Boolean> run(final File script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public Future<Boolean> run(final URI script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public Future<Object> run(final String script) {
		return this.browserScriptRunner.run(script);
	}

	@Override
	public <DEST> Future<DEST> run(final String script,
			final IConverter<Object, DEST> converter) {
		return this.browserScriptRunner.run(script, converter);
	}

	@Override
	public <DEST> DEST runImmediately(String script,
			IConverter<Object, DEST> converter) throws Exception {
		return this.browserScriptRunner.runImmediately(script, converter);
	}

	@Override
	public void runImmediately(File script) throws Exception {
		this.browserScriptRunner.runImmediately(script);
	}

	@Override
	public void scriptAboutToBeSentToBrowser(String script) {
		return;
	}

	@Override
	public void scriptReturnValueReceived(Object returnValue) {
		return;
	}

	@Override
	public Future<Void> injectCssFile(URI uri) {
		return this
				.run("if(document.createStyleSheet){document.createStyleSheet(\""
						+ uri.toString()
						+ "\")}else{ var link=document.createElement(\"link\"); link.rel=\"stylesheet\"; link.type=\"text/css\"; link.href=\""
						+ uri.toString()
						+ "\"; document.getElementsByTagName(\"head\")[0].appendChild(link); }",
						IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<Void> injectCss(String css) {
		String script = "(function(){var style=document.createElement(\"style\");style.appendChild(document.createTextNode(\""
				+ css
				+ "\"));(document.getElementsByTagName(\"head\")[0]||document.documentElement).appendChild(style)})()";
		return this.run(script, IConverter.CONVERTER_VOID);
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
	public void addMouseListener(IMouseListener mouseListener) {
		this.mouseListeners.add(mouseListener);
	}

	@Override
	public void removeMouseListener(IMouseListener mouseListener) {
		this.mouseListeners.remove(mouseListener);
	}

	/**
	 * 
	 * @param string
	 * @param mouseEnter
	 *            true if mouseenter; false otherwise
	 */
	protected void fireAnkerHover(String html, boolean mouseEnter) {
		IAnker anker = BrowserUtils.extractAnker(html);
		for (IAnkerListener ankerListener : Browser.this.ankerListeners) {
			ankerListener.ankerHovered(anker, mouseEnter);
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	protected void fireMouseMove(double x, double y) {
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.mouseMove(x, y);
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	protected void fireMouseDown(double x, double y) {
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.mouseDown(x, y);
		}
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	protected void fireMouseUp(double x, double y) {
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.mouseUp(x, y);
		}
	}

	/**
	 * 
	 * @param string
	 */
	protected void fireAnkerClicked(String html) {
		IAnker anker = BrowserUtils.extractAnker(html);
		for (IAnkerListener ankerListener : Browser.this.ankerListeners) {
			ankerListener.ankerClicked(anker);
		}
	}

	@Override
	public void addFocusListener(IFocusListener focusListener) {
		this.focusListeners.add(focusListener);
	}

	@Override
	public void removeFocusListener(IFocusListener focusListener) {
		this.focusListeners.remove(focusListener);
	}

	synchronized protected void fireFocusGained(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusGained(element);
		}
	}

	synchronized protected void fireFocusLost(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusLost(element);
		}
	}

	@Override
	public void addDropListener(IDropListener dropListener) {
		this.dropListeners.add(dropListener);
	}

	@Override
	public void removeDropListener(IDropListener dropListener) {
		this.dropListeners.remove(dropListener);
	}

	synchronized protected void fireDrop(long offsetX, long offsetY, String data) {
		for (IDropListener dropListener : this.dropListeners) {
			dropListener.drop(offsetX, offsetY, data);
		}
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

	private String escape(String html) {
		return html.replace("\n", "<br>").replace("&#xD;", "")
				.replace("\r", "").replace("\"", "\\\"").replace("'", "\\'");
	}

	@Override
	public Future<Void> setBodyHtml(String html) {
		return this.run("document.body.innerHTML = ('" + this.escape(html)
				+ "');", IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<String> getBodyHtml() {
		return this.run("return document.body.innerHTML",
				IConverter.CONVERTER_STRING);
	}

	@Override
	public Future<String> getHtml() {
		return this.run("return document.documentElement.outerHTML",
				IConverter.CONVERTER_STRING);
	}

	@Override
	public Future<Object> pasteHtmlAtCaret(String html) {
		String escapedHtml = this.escape(html);
		return this
				.run("if(['input','textarea'].indexOf(document.activeElement.tagName.toLowerCase()) != -1) { document.activeElement.value = '"
						+ escapedHtml
						+ "';} else { var t,n;if(window.getSelection){t=window.getSelection();if(t.getRangeAt&&t.rangeCount){n=t.getRangeAt(0);n.deleteContents();var r=document.createElement(\"div\");r.innerHTML='"
						+ escapedHtml
						+ "';var i=document.createDocumentFragment(),s,o;while(s=r.firstChild){o=i.appendChild(s)}n.insertNode(i);if(o){n=n.cloneRange();n.setStartAfter(o);n.collapse(true);t.removeAllRanges();t.addRange(n)}}}else if(document.selection&&document.selection.type!=\"Control\"){document.selection.createRange().pasteHTML('"
						+ escapedHtml + "')}}");
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		try {
			this.browser
					.execute("if (window[\"__notifySize\"] && typeof window[\"__notifySize\"]) window[\"__notifySize\"]();");
		} catch (Exception e) {
			LOGGER.error("Error computing size for "
					+ Browser.class.getSimpleName());
		}

		Rectangle bounds = this.cachedContentBounds;
		if (bounds == null) {
			return super.computeSize(wHint, hHint, changed);
		}

		return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
	}
}
