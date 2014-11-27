package com.bkahlert.nebula.widgets.browser;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.EventDelegator;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.HandlerUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.exception.JavaScriptException;
import com.bkahlert.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.nebula.widgets.browser.extended.html.Element;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IDNDListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner.BrowserStatus;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner.JavaScriptExceptionListener;

public class Browser extends Composite implements IBrowser {

	private static Logger LOGGER = Logger.getLogger(Browser.class);

	private static final int STYLES = SWT.INHERIT_FORCE;

	public static final String FOCUS_CONTROL_ID = "com.bkahlert.nebula.browser";

	private org.eclipse.swt.browser.Browser browser;
	private BrowserScriptRunner browserScriptRunner;

	private boolean initWithSystemBackgroundColor;
	private boolean textSelectionsDisabled = false;
	private boolean settingUri = false;
	private boolean allowLocationChange = false;
	private Rectangle cachedContentBounds = null;

	private final List<IAnkerListener> ankerListeners = new ArrayList<IAnkerListener>();
	private final List<IMouseListener> mouseListeners = new ArrayList<IMouseListener>();
	private final List<IFocusListener> focusListeners = new ArrayList<IFocusListener>();
	private final List<IDNDListener> dndListeners = new ArrayList<IDNDListener>();
	private final List<JavaScriptExceptionListener> javaScriptExceptionListeners = Collections
			.synchronizedList(new ArrayList<JavaScriptExceptionListener>());

	/**
	 * Constructs a new {@link Browser} with the given styles.
	 *
	 * @param parent
	 * @param style
	 *            if {@link SWT#INHERIT_FORCE}) is set the loaded page's
	 *            background is replaced by the inherited background color
	 */
	public Browser(Composite parent, int style) {
		super(parent, style | SWT.EMBEDDED & ~STYLES);
		this.setLayout(new FillLayout());
		this.initWithSystemBackgroundColor = (style & SWT.INHERIT_FORCE) != 0;

		this.browser = new org.eclipse.swt.browser.Browser(this, SWT.NONE);
		this.browser.setVisible(false);
		this.browserScriptRunner = new BrowserScriptRunner(this.browser,
				javaScriptException -> Browser.this
						.fireJavaScriptExceptionThrown(javaScriptException)) {
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
				if (arguments.length == 3
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)
						&& (arguments[2] == null || arguments[2] instanceof String)) {
					Browser.this.fireMouseDown((Double) arguments[0],
							(Double) arguments[1], (String) arguments[2]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__mouseup") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 3
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)
						&& (arguments[2] == null || arguments[2] instanceof String)) {
					Browser.this.fireMouseUp((Double) arguments[0],
							(Double) arguments[1], (String) arguments[2]);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__click") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 3
						&& (arguments[0] == null || arguments[0] instanceof Double)
						&& (arguments[1] == null || arguments[1] instanceof Double)
						&& (arguments[2] == null || arguments[2] instanceof String)) {
					Browser.this.fireClicked((Double) arguments[0],
							(Double) arguments[1], (String) arguments[2]);
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
									: 0,
							arguments[1] != null ? (int) Math
									.round((Double) arguments[1]) : 0,
							arguments[2] != null ? (int) Math
									.round((Double) arguments[2])
									: Integer.MAX_VALUE,
							arguments[3] != null ? (int) Math
									.round((Double) arguments[3])
									: Integer.MAX_VALUE);
					LOGGER.debug("browser content resized to "
							+ Browser.this.cachedContentBounds);
					Composite root = SWTUtils.getRoot(Browser.this);
					LOGGER.debug("layout all");
					root.layout(true, true);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__dragStart") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 5 && arguments[0] instanceof Double
						&& arguments[1] instanceof Double
						&& arguments[2] instanceof String
						&& arguments[3] instanceof String
						&& arguments[4] instanceof String) {
					long offsetX = Math.round((Double) arguments[0]);
					long offsetY = Math.round((Double) arguments[1]);
					IElement element = BrowserUtils
							.extractElement((String) arguments[2]);
					String mimeType = (String) arguments[3];
					String data = (String) arguments[4];
					Browser.this.fireDragStart(offsetX, offsetY, element,
							mimeType, data);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__drop") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 5 && arguments[0] instanceof Double
						&& arguments[1] instanceof Double
						&& arguments[2] instanceof String
						&& arguments[3] instanceof String
						&& arguments[4] instanceof String) {
					long offsetX = Math.round((Double) arguments[0]);
					long offsetY = Math.round((Double) arguments[1]);
					IElement element = BrowserUtils
							.extractElement((String) arguments[2]);
					String mimeType = (String) arguments[3];
					String data = (String) arguments[4];
					Browser.this.fireDrop(offsetX, offsetY, element, mimeType,
							data);
				}
				return null;
			}
		};
		new BrowserFunction(this.browser, "__consoleLog") {
			@Override
			public Object function(Object[] arguments) {
				LOGGER.debug(StringUtils.join(arguments, ", "));
				return null;
			}
		};
		new BrowserFunction(this.browser, "__consoleError") {
			@Override
			public Object function(Object[] arguments) {
				LOGGER.error(StringUtils.join(arguments, ", "));
				return null;
			}
		};

		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				if (!Browser.this.settingUri) {
					event.doit = Browser.this.allowLocationChange
							|| Browser.this.browserScriptRunner
									.getBrowserStatus() == BrowserStatus.LOADING;
				}
			}
		});

		HandlerUtils.activateCustomPasteHandlerConsideration(this.browser,
				FOCUS_CONTROL_ID, "application/x-java-file-list", "image");
		this.addDisposeListener((DisposeListener) e -> HandlerUtils
				.deactivateCustomPasteHandlerConsideration(Browser.this.browser));

		this.addDisposeListener((DisposeListener) e -> {
			synchronized (Browser.this.monitor) {
				if (Browser.this.browserScriptRunner.getBrowserStatus() == BrowserStatus.LOADING) {
					Browser.this.browserScriptRunner
							.setBrowserStatus(BrowserStatus.DISPOSED);
				}
				Browser.this.monitor.notifyAll();
			}
		});
	}

	private boolean eventCatchScriptInjected = false;

	/**
	 * Injects the code needed for {@link #addAnkerListener(IAnkerListener)},
	 * {@link #addFokusListener(IFocusListener)} and
	 * {@link #addDNDListener(IFocusListener)} to work.
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
			Browser.this.runContentsImmediately(events);
		} catch (Exception e) {
			if (e.getCause() instanceof SWTException) {
				// disposed
			} else {
				LOGGER.error("Could not inject events catch script in "
						+ Browser.this.getClass().getSimpleName(), e);
			}
		}

		File dnd = BrowserUtils.getFile(Browser.class, "dnd.js");
		File dndCss = BrowserUtils.getFile(Browser.class, "dnd.css");
		try {
			Browser.this.runContentsImmediately(dnd);
			Browser.this.injectCssFile(new URI("file://" + dndCss));
		} catch (Exception e) {
			if (e.getCause() instanceof SWTException) {
				// disposed
			} else {
				LOGGER.error("Could not inject drop catch script in "
						+ Browser.this.getClass().getSimpleName(), e);
			}
		}

		Browser.this.eventCatchScriptInjected = true;
	}

	/**
	 * This method waits for the {@link Browser} to complete loading.
	 * <p>
	 * It has been observed that the
	 * {@link ProgressListener#completed(ProgressEvent)} fires to early. This
	 * method uses JavaScript to reliably detect the completed state.
	 *
	 * @param pageLoadCheckExpression
	 */
	private void waitAndComplete(String pageLoadCheckExpression) {
		if (Browser.this.browser == null || Browser.this.browser.isDisposed()) {
			return;
		}

		if (Browser.this.browserScriptRunner.getBrowserStatus() != BrowserStatus.LOADING) {
			if (!Arrays
					.asList(BrowserStatus.TIMEDOUT, BrowserStatus.DISPOSED)
					.contains(
							Browser.this.browserScriptRunner.getBrowserStatus())) {
				LOGGER.error("State Error: "
						+ Browser.this.browserScriptRunner.getBrowserStatus());
			}
			return;
		}

		String completedCallbackFunctionName = BrowserUtils
				.createRandomFunctionName();

		String completedCheckScript = "(function() { "
				+ "function test() { if(document.readyState == 'complete'"
				+ (pageLoadCheckExpression != null ? " && ("
						+ pageLoadCheckExpression + ")" : "") + ") { "
				+ completedCallbackFunctionName
				+ "(); } else { window.setTimeout(test, 50); } } "
				+ "test(); })()";

		final AtomicReference<BrowserFunction> completedCallback = new AtomicReference<BrowserFunction>();
		completedCallback.set(new BrowserFunction(this.browser,
				completedCallbackFunctionName) {
			@Override
			public Object function(Object[] arguments) {
				Browser.this.complete();
				completedCallback.get().dispose();
				return null;
			}
		});

		try {
			this.runImmediately(completedCheckScript, IConverter.CONVERTER_VOID);
		} catch (Exception e) {
			LOGGER.error(
					"An error occurred while checking the page load state", e);
			synchronized (Browser.this.monitor) {
				Browser.this.monitor.notifyAll();
			}
		}
	}

	/**
	 * This method is called by {@link #waitAndComplete(String)} and post
	 * processes the loaded page.
	 * <ol>
	 * <li>calls {@link #beforeCompletion(String)}</li>
	 * <li>injects necessary scripts</li>
	 * <li>runs the scheduled user scripts</li>
	 * </ol>
	 */
	private void complete() {
		final String uri = Browser.this.browser.getUrl();
		if (this.initWithSystemBackgroundColor) {
			this.setBackground(SWTUtils.getEffectiveBackground(Browser.this));
		}
		if (this.textSelectionsDisabled) {
			try {
				this.injectCssImmediately("* { -webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none; }");
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		final Future<Void> finished = Browser.this.beforeCompletion(uri);
		ExecUtils.nonUISyncExec(
				Browser.class,
				"Progress Check for " + uri,
				() -> {
					try {
						if (finished != null) {
							finished.get();
						}
					} catch (Exception e) {
						LOGGER.error(e);
					}

					Browser.this.injectEventCatchScript();
					ExecUtils.asyncExec(() -> {
						if (!Browser.this.browser.isDisposed()) {
							Browser.this.browser.setVisible(true);
						}
					});
					synchronized (Browser.this.monitor) {
						if (!Arrays.asList(BrowserStatus.TIMEDOUT,
								BrowserStatus.DISPOSED).contains(
								Browser.this.browserScriptRunner
										.getBrowserStatus())) {
							Browser.this.browserScriptRunner
									.setBrowserStatus(BrowserStatus.LOADED);
						}
						Browser.this.monitor.notifyAll();
					}
				});
	}

	@Override
	public Future<Boolean> open(final String uri, final Integer timeout,
			final String pageLoadCheckExpression) {
		if (this.browser.isDisposed()) {
			throw new SWTException(SWT.ERROR_WIDGET_DISPOSED);
		}

		Browser.this.browserScriptRunner
				.setBrowserStatus(BrowserStatus.LOADING);

		this.browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				Browser.this.waitAndComplete(pageLoadCheckExpression);
			}
		});

		return ExecUtils
				.nonUIAsyncExec(
						Browser.class,
						"Opening " + uri,
						(Callable<Boolean>) () -> {
							// stops waiting after timeout
							Future<Void> timeoutMonitor = null;
							if (timeout != null && timeout > 0) {
								timeoutMonitor = ExecUtils
										.nonUIAsyncExec(
												Browser.class,
												"Timeout Watcher for " + uri,
												() -> {
													synchronized (Browser.this.monitor) {
														if (Browser.this.browserScriptRunner
																.getBrowserStatus() != BrowserStatus.LOADED) {
															Browser.this.browserScriptRunner
																	.setBrowserStatus(BrowserStatus.TIMEDOUT);
														}
														Browser.this.monitor
																.notifyAll();
													}
												}, timeout);
							} else {
								LOGGER.warn("timeout must be greater or equal 0. Ignoring timeout.");
							}

							Browser.this.beforeLoad(uri);

							ExecUtils.syncExec(() -> {
								Browser.this.settingUri = true;
								if (!Browser.this.browser.isDisposed()) {
									Browser.this.browser.setUrl(uri.toString());
								}
								Browser.this.settingUri = false;
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
									// notified by progresslistener or by
									// timeout
								}

								if (timeoutMonitor != null) {
									timeoutMonitor.cancel(true);
								}

								switch (Browser.this.browserScriptRunner
										.getBrowserStatus()) {
								case LOADED:
									LOGGER.debug("Successfully loaded " + uri);
									break;
								case TIMEDOUT:
									LOGGER.warn("Aborted loading " + uri
											+ " due to timeout");
									break;
								case DISPOSED:
									if (!Browser.this.browser.isDisposed()) {
										LOGGER.info("Aborted loading " + uri
												+ " due to disposal");
									}
									break;
								default:
									throw new RuntimeException(
											"Implementation error");
								}

								return Browser.this.browserScriptRunner
										.getBrowserStatus() == BrowserStatus.LOADED;
							}
						});
	}

	@Override
	public Future<Boolean> open(String address, Integer timeout) {
		return this.open(address, timeout, null);
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout) {
		return this.open(uri.toString(), timeout, null);
	}

	@Override
	public Future<Boolean> open(URI uri, Integer timeout,
			String pageLoadCheckExpression) {
		return this.open(uri.toString(), timeout, pageLoadCheckExpression);
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
		this.browser.addListener(SWT.MenuDetect, event -> event.doit = false);
	}

	public void deactivateTextSelections() {
		this.textSelectionsDisabled = true;
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
	public void runContentsImmediately(File scriptFile) throws Exception {
		this.browserScriptRunner.runContentsImmediately(scriptFile);
	}

	@Override
	public void runContentsAsScriptTagImmediately(File scriptFile)
			throws Exception {
		this.browserScriptRunner.runContentsAsScriptTagImmediately(scriptFile);
	}

	@Override
	public void scriptAboutToBeSentToBrowser(String script) {
		return;
	}

	@Override
	public void scriptReturnValueReceived(Object returnValue) {
		return;
	}

	private static String createJsFileInjectionScript(File file) {
		return "var script=document.createElement(\"script\"); script.type=\"text/javascript\"; script.src=\""
				+ "file://"
				+ file
				+ "\"; document.getElementsByTagName(\"head\")[0].appendChild(script);";
	}

	@Override
	public Future<Void> injectJsFile(File file) {
		return this.run(createJsFileInjectionScript(file),
				IConverter.CONVERTER_VOID);
	}

	@Override
	public void injectJsFileImmediately(File file) throws Exception {
		this.runImmediately(createJsFileInjectionScript(file),
				IConverter.CONVERTER_VOID);
	}

	private static String createCssFileInjectionScript(URI uri) {
		return "if(document.createStyleSheet){document.createStyleSheet(\""
				+ uri.toString()
				+ "\")}else{var link=document.createElement(\"link\"); link.rel=\"stylesheet\"; link.type=\"text/css\"; link.href=\""
				+ uri.toString()
				+ "\"; document.getElementsByTagName(\"head\")[0].appendChild(link); }";
	}

	@Override
	public Future<Void> injectCssFile(URI uri) {
		return this.run(createCssFileInjectionScript(uri),
				IConverter.CONVERTER_VOID);
	}

	public void injectCssFileImmediately(URI uri) throws Exception {
		this.runImmediately(createCssFileInjectionScript(uri),
				IConverter.CONVERTER_VOID);
	}

	private static String createCssInjectionScript(String css) {
		return "(function(){var style=document.createElement(\"style\");style.appendChild(document.createTextNode(\""
				+ css
				+ "\"));(document.getElementsByTagName(\"head\")[0]||document.documentElement).appendChild(style)})()";
	}

	@Override
	public Future<Void> injectCss(String css) {
		return this.run(createCssInjectionScript(css),
				IConverter.CONVERTER_VOID);
	}

	@Override
	public void injectCssImmediately(String css) throws Exception {
		this.runImmediately(createCssInjectionScript(css),
				IConverter.CONVERTER_VOID);
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
		IElement element = BrowserUtils.extractElement(html);
		IAnker anker = new Anker(element.getAttributes(), element.getContent());
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
	 * @param element
	 *            on which the mouse went down
	 */
	protected void fireMouseDown(double x, double y, String html) {
		IElement element = BrowserUtils.extractElement(html);
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.mouseDown(x, y, element);
		}
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @param arguments
	 *            on which the mouse went up
	 */
	protected void fireMouseUp(double x, double y, String html) {
		IElement element = BrowserUtils.extractElement(html);
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.mouseUp(x, y, element);
		}
	}

	/**
	 *
	 * @param y
	 * @param x
	 * @param string
	 */
	protected void fireClicked(Double x, Double y, String html) {
		IElement element = BrowserUtils.extractElement(html);
		for (IMouseListener mouseListener : Browser.this.mouseListeners) {
			mouseListener.clicked(x, y, element);
		}
		if (element.getName().equals("a")) {
			IAnker anker = new Anker(element.getAttributes(),
					element.getContent());
			for (IAnkerListener ankerListener : Browser.this.ankerListeners) {
				ankerListener.ankerClicked(anker);
			}
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

	protected void fireFocusGained(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusGained(element);
		}
	}

	protected void fireFocusLost(IElement element) {
		for (IFocusListener focusListener : this.focusListeners) {
			focusListener.focusLost(element);
		}
	}

	@Override
	public void addDNDListener(IDNDListener dndListener) {
		this.dndListeners.add(dndListener);
	}

	@Override
	public void removeDNDListener(IDNDListener dndListener) {
		this.dndListeners.remove(dndListener);
	}

	synchronized protected void fireDragStart(long offsetX, long offsetY,
			IElement element, String mimeType, String data) {
		for (IDNDListener dndListener : this.dndListeners) {
			dndListener.dragStart(offsetX, offsetY, element, mimeType, data);
		}
	}

	synchronized protected void fireDrop(long offsetX, long offsetY,
			IElement element, String mimeType, String data) {
		for (IDNDListener dndListener : this.dndListeners) {
			dndListener.drop(offsetX, offsetY, element, mimeType, data);
		}
	}

	@Override
	public void addJavaScriptExceptionListener(
			JavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.add(javaScriptExceptionListener);
	}

	@Override
	public void removeJavaScriptExceptionListener(
			JavaScriptExceptionListener javaScriptExceptionListener) {
		this.javaScriptExceptionListeners.remove(javaScriptExceptionListener);
	}

	synchronized protected void fireJavaScriptExceptionThrown(
			JavaScriptException javaScriptException) {
		for (JavaScriptExceptionListener JavaScriptExceptionListener : this.javaScriptExceptionListeners) {
			JavaScriptExceptionListener.thrown(javaScriptException);
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
	public void setBackground(Color color) {
		super.setBackground(color);
		String hex = color != null ? new RGB(color.getRGB()).toDecString()
				: "transparent";
		try {
			this.injectCssImmediately("html, body { background-color: " + hex
					+ "; }");
		} catch (Exception e) {
			LOGGER.error("Error setting background color to " + color, e);
		}
	}

	@Override
	public Future<Void> pasteHtmlAtCaret(String html) {
		String escapedHtml = this.escape(html);
		try {
			File js = File.createTempFile("paste", ".js");
			FileUtils
					.write(js,
							"if(['input','textarea'].indexOf(document.activeElement.tagName.toLowerCase()) != -1) { document.activeElement.value = '");
			FileOutputStream outStream = new FileOutputStream(js, true);
			IOUtils.copy(IOUtils.toInputStream(escapedHtml), outStream);
			IOUtils.copy(
					IOUtils.toInputStream("';} else { var t,n;if(window.getSelection){t=window.getSelection();if(t.getRangeAt&&t.rangeCount){n=t.getRangeAt(0);n.deleteContents();var r=document.createElement(\"div\");r.innerHTML='"),
					outStream);
			IOUtils.copy(IOUtils.toInputStream(escapedHtml), outStream);
			IOUtils.copy(
					IOUtils.toInputStream("';var i=document.createDocumentFragment(),s,o;while(s=r.firstChild){o=i.appendChild(s)}n.insertNode(i);if(o){n=n.cloneRange();n.setStartAfter(o);n.collapse(true);t.removeAllRanges();t.addRange(n)}}}else if(document.selection&&document.selection.type!=\"Control\"){document.selection.createRange().pasteHTML('"),
					outStream);
			IOUtils.copy(IOUtils.toInputStream(escapedHtml + "')}}"), outStream);
			return this.injectJsFile(js);
		} catch (Exception e) {
			return new CompletedFuture<Void>(null, e);
		}
	}

	@Override
	public Future<Void> addFocusBorder() {
		return this
				.run("window.__addFocusBorder();", IConverter.CONVERTER_VOID);
	}

	@Override
	public Future<Void> removeFocusBorder() {
		return this.run("window.__removeFocusBorder();",
				IConverter.CONVERTER_VOID);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Rectangle bounds = this.cachedContentBounds;
		if (bounds == null) {
			return super.computeSize(wHint, hHint, changed);
		}
		Point size = new Point(bounds.x + bounds.width, bounds.y
				+ bounds.height);
		LOGGER.debug(Browser.class.getSimpleName() + ".computeSize(" + wHint
				+ ", " + hHint + ", " + changed + ") -> " + size);
		return size;
	}
}
