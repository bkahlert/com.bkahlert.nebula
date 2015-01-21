package com.bkahlert.nebula.widgets.browser.runner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.Debouncer;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.OffWorker;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.exception.BrowserTimeoutException;
import com.bkahlert.nebula.widgets.browser.exception.BrowserUninitializedException;
import com.bkahlert.nebula.widgets.browser.exception.JavaScriptException;
import com.bkahlert.nebula.widgets.browser.exception.ScriptExecutionException;
import com.bkahlert.nebula.widgets.browser.exception.UnexpectedBrowserStateException;

/**
 * This is the default implementation of {@link IBrowserScriptRunner}.
 *
 * @author bkahlert
 *
 */
public class BrowserScriptRunner implements IBrowserScriptRunner {

	/**
	 * Relevant statuses a browser can have in terms of script execution.
	 *
	 * @author bkahlert
	 *
	 */
	public static enum BrowserStatus {
		/**
		 * The browser is initializing, meaning no resource had been loaded,
		 * yet.
		 */
		INITIALIZING,

		/**
		 * The browser is currently loading a resource.
		 */
		LOADING,

		/**
		 * The browser has successfully loaded a resource.
		 */
		LOADED,

		/**
		 * The browser timed out.
		 */
		TIMEDOUT,

		/**
		 * The browser is currently disposing or already disposed.
		 */
		DISPOSED
	}

	/**
	 * Instances of this class can handle asynchronous
	 * {@link JavaScriptException}s, that are exceptions raised by the
	 * {@link Browser} itself and not provoked by Java invocations.
	 *
	 * @author bkahlert
	 *
	 */
	public static interface JavaScriptExceptionListener {
		public void thrown(JavaScriptException javaScriptException);
	}

	private static final Logger LOGGER = Logger
			.getLogger(BrowserScriptRunner.class);

	private static <DEST> Callable<DEST> createScriptRunner(
			final BrowserScriptRunner browserScriptRunner, final String script,
			final IConverter<Object, DEST> converter) {
		final String label = StringUtils.shorten(script);
		return ExecUtils.createThreadLabelingCode(
				(Callable<DEST>) () -> {
					if (browserScriptRunner.browser == null
							|| browserScriptRunner.browser.isDisposed()) {
						throw new ScriptExecutionException(script,
								new SWTException(SWT.ERROR_WIDGET_DISPOSED));
					}
					LOGGER.info("Running " + label);
					try {
						browserScriptRunner
								.scriptAboutToBeSentToBrowser(script);
						Object returnValue = browserScriptRunner.browser
								.evaluate(BrowserUtils
										.getExecutionReturningScript(script));

						BrowserUtils.assertException(script, returnValue);

						browserScriptRunner
								.scriptReturnValueReceived(returnValue);
						DEST rs = converter.convert(returnValue);
						LOGGER.info("Returned " + rs);
						return rs;
					} catch (SWTException e1) {
						throw e1;
					} catch (JavaScriptException e2) {
						LOGGER.error(e2);
						throw e2;
					} catch (Exception e3) {
						LOGGER.error(e3);
						throw e3;
					}
				}, Browser.class, "Running " + label);
	}

	private final org.eclipse.swt.browser.Browser browser;
	private BrowserStatus browserStatus;

	private final OffWorker delayedScriptsWorker = new OffWorker(
			this.getClass(), "Script Runner");

	public BrowserScriptRunner(Browser browser,
			final JavaScriptExceptionListener javaScriptExceptionListener) {
		Assert.isNotNull(browser);
		this.browser = browser;
		this.browserStatus = BrowserStatus.INITIALIZING;

		// throws exception that arise from calls within the browser,
		// meaning code that has not been invoked by Java but by JavaScript
		new BrowserFunction(browser, "__error_callback") {
			@Override
			public Object function(Object[] arguments) {
				JavaScriptException javaScriptException = BrowserUtils
						.parseJavaScriptException(arguments);
				LOGGER.error(javaScriptException);
				if (javaScriptExceptionListener != null) {
					javaScriptExceptionListener.thrown(javaScriptException);
				}
				return false;
			}
		};
	}

	/**
	 * Sets the {@link BrowserStatus}. This information is necessary for the
	 * correct script execution.
	 *
	 * @param browserStatus
	 * @throws UnexpectedBrowserStateException
	 */
	public void setBrowserStatus(BrowserStatus browserStatus)
			throws UnexpectedBrowserStateException {
		Assert.isNotNull(browserStatus);
		if (this.browserStatus == browserStatus) {
			return;
		}

		// throw exception on invalid new status
		switch (this.browserStatus) {
		case INITIALIZING:
			if (Arrays.asList(BrowserStatus.TIMEDOUT, BrowserStatus.DISPOSED)
					.contains(browserStatus)) {
				throw new UnexpectedBrowserStateException("Cannot switch from "
						+ this.browserStatus + " to " + browserStatus);
			}
			break;
		case LOADING:
			if (browserStatus == BrowserStatus.INITIALIZING) {
				throw new UnexpectedBrowserStateException("Cannot switch from "
						+ this.browserStatus + " to " + browserStatus);
			}
			break;
		case LOADED:
			throw new UnexpectedBrowserStateException("Cannot switch from "
					+ this.browserStatus + " to " + browserStatus);
		case TIMEDOUT:
			throw new UnexpectedBrowserStateException("Cannot switch from "
					+ this.browserStatus + " to " + browserStatus);
		case DISPOSED:
			throw new UnexpectedBrowserStateException("Cannot switch from "
					+ this.browserStatus + " to " + browserStatus);
		default:
			throw new UnexpectedBrowserStateException("Cannot switch from "
					+ this.browserStatus + " to " + browserStatus);
		}

		// apply new status
		this.browserStatus = browserStatus;
		switch (this.browserStatus) {
		case LOADING:
			this.activateExceptionHandling();
			break;
		case LOADED:
			this.delayedScriptsWorker.start();
			this.delayedScriptsWorker.finish();
			break;
		case TIMEDOUT:
		case DISPOSED:
			if (this.delayedScriptsWorker != null) {
				this.delayedScriptsWorker.submit(() -> {
					if (!BrowserScriptRunner.this.delayedScriptsWorker
							.isShutdown()) {
						BrowserScriptRunner.this.delayedScriptsWorker
								.shutdown();
					}
					return null;
				});
			}

			this.delayedScriptsWorker.start();
			this.delayedScriptsWorker.finish();
			break;
		default:
		}
	}

	/**
	 * Returns the previously set {@link BrowserStatus}.
	 *
	 * @return
	 */
	public BrowserStatus getBrowserStatus() {
		return this.browserStatus;
	}

	/**
	 * Notifies all registered {@link IJavaScriptExceptionListener}s in case a
	 * JavaScript error occurred.
	 */
	private void activateExceptionHandling() {
		try {
			this.runImmediately(BrowserUtils
					.getExceptionForwardingScript("__error_callback"),
					IConverter.CONVERTER_VOID);
		} catch (Exception e) {
			LOGGER.error(
					"Error activating browser's exception handling. JavaScript exceptions are not detected!",
					e);
		}
	}

	@Override
	public Future<Boolean> inject(URI script) {
		return this.run(script, false);
	}

	@Override
	public Future<Boolean> run(final File script) {
		Assert.isLegal(script.canRead());
		try {
			return this.run(new URI("file://" + script.getAbsolutePath()));
		} catch (URISyntaxException e) {
			return new CompletedFuture<Boolean>(false, e);
		}
	}

	@Override
	public Future<Boolean> run(final URI script) {
		return this.run(script, true);
	}

	private Future<Boolean> run(final URI script,
			final boolean removeAfterExecution) {
		Assert.isLegal(script != null);

		if ("file".equalsIgnoreCase(script.getScheme())) {
			File file = new File(script.toString()
					.substring("file://".length()));
			try {
				String scriptContent = FileUtils.readFileToString(file);
				Future<Boolean> rs = this.run(scriptContent,
						returnValue -> true);
				if (removeAfterExecution) {
					LOGGER.warn("The script "
							+ script
							+ " is on the local file system. To circument security restrictions its content becomes directly executed and thus cannot be removed.");
				}
				return rs;
			} catch (IOException e) {
				return new CompletedFuture<Boolean>(null, e);
			}
		} else {
			return ExecUtils
					.nonUIAsyncExec(
							Browser.class,
							"Script Runner for: " + script,
							(Callable<Boolean>) () -> {
								final String callbackFunctionName = BrowserUtils
										.createRandomFunctionName();

								final Semaphore mutex = new Semaphore(0);
								ExecUtils
										.syncExec(() -> {
											final AtomicReference<BrowserFunction> callback = new AtomicReference<BrowserFunction>();
											callback.set(new BrowserFunction(
													BrowserScriptRunner.this.browser,
													callbackFunctionName) {
												@Override
												public Object function(
														Object[] arguments) {
													callback.get().dispose();
													mutex.release();
													return null;
												}
											});
										});

								String js = "var h = document.getElementsByTagName(\"head\")[0]; var s = document.createElement(\"script\");s.type = \"text/javascript\";s.src = \""
										+ script.toString()
										+ "\"; s.onload=function(e){";
								if (removeAfterExecution) {
									js += "h.removeChild(s);";
								}
								js += callbackFunctionName + "();";
								js += "};h.appendChild(s);";

								// runs the scripts that ends by calling the
								// callback
								// ...
								BrowserScriptRunner.this.run(js);
								try {
									// ... which destroys itself and releases
									// this
									// lock
									mutex.acquire();
								} catch (InterruptedException e) {
									LOGGER.error(e);
								}
								return null;
							});
		}
	}

	@Override
	public Future<Object> run(final String script) {
		return this.run(script, object -> object);
	}

	private Map<String, Debouncer<String>> debouncers = new HashMap<>();

	@Override
	public void run(String script, long interval, String scope) {
		Assert.isLegal(scope != null && script != null);
		if (!this.debouncers.containsKey(scope)) {
			this.debouncers.put(scope, new Debouncer<>(script1 -> {
				BrowserScriptRunner.this.run(script1.toString());
			}));
		}
		this.debouncers.get(scope).call(script, interval);
	}

	@Override
	public <DEST> Future<DEST> run(final String script,
			final IConverter<Object, DEST> converter) {
		Assert.isLegal(converter != null);
		final Callable<DEST> scriptRunner = createScriptRunner(this, script,
				converter);

		switch (this.browserStatus) {
		case INITIALIZING:
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(script,
							new BrowserUninitializedException(this.browser)));
		case LOADING:
			return this.delayedScriptsWorker.submit(() -> {
				switch (BrowserScriptRunner.this.browserStatus) {
				case LOADED:
					return ExecUtils.syncExec(scriptRunner);
				case TIMEDOUT:
					throw new ScriptExecutionException(script,
							new BrowserTimeoutException());
				case DISPOSED:
					throw new ScriptExecutionException(script,
							new SWTException(SWT.ERROR_WIDGET_DISPOSED));
				default:
					throw new ScriptExecutionException(script,
							new UnexpectedBrowserStateException(
									BrowserScriptRunner.this.browserStatus
											.toString()));
				}
			});
		case LOADED:
			try {
				return new CompletedFuture<DEST>(this.runImmediately(script,
						converter), null);
			} catch (Exception e) {
				return new CompletedFuture<DEST>(null, e);
			}
		case TIMEDOUT:
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(script,
							new BrowserTimeoutException()));
		case DISPOSED:
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(script, new SWTException(
							SWT.ERROR_WIDGET_DISPOSED)));
		default:
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(script,
							new UnexpectedBrowserStateException(
									this.browserStatus.toString())));
		}
	}

	@Override
	public <DEST> DEST runImmediately(String script,
			IConverter<Object, DEST> converter) throws Exception {
		return ExecUtils.syncExec(createScriptRunner(this, script, converter));
	}

	@Override
	public void runContentsImmediately(File script) throws Exception {
		String scriptContent = FileUtils.readFileToString(script);
		this.runImmediately(scriptContent, IConverter.CONVERTER_VOID);
	}

	@Override
	public void runContentsAsScriptTagImmediately(File scriptFile)
			throws Exception {
		String scriptContent = FileUtils.readFileToString(scriptFile);
		String script = "var script=document.createElement(\"script\"); script.type=\"text/javascript\"; script.text=\""
				+ StringEscapeUtils.escapeJavaScript(scriptContent)
				+ "\"; document.getElementsByTagName(\"head\")[0].appendChild(script);";
		this.runImmediately(script, IConverter.CONVERTER_VOID);
	}

	@Override
	public void scriptAboutToBeSentToBrowser(String script) {
		return;
	}

	@Override
	public void scriptReturnValueReceived(Object returnValue) {
		return;
	}
}
