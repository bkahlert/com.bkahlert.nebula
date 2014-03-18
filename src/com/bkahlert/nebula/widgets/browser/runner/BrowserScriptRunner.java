package com.bkahlert.nebula.widgets.browser.runner;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.ui.services.IDisposable;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.OffWorker;
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
public class BrowserScriptRunner implements IBrowserScriptRunner, IDisposable {

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
		 * The browser cancelled loading of a resource.
		 */
		CANCELLED;
	}

	private static final Logger LOGGER = Logger
			.getLogger(BrowserScriptRunner.class);

	private static <DEST> Callable<DEST> createScriptRunner(
			final BrowserScriptRunner browserScriptRunner, final String script,
			final IConverter<Object, DEST> converter) {
		return ExecUtils.createThreadLabelingCode(new Callable<DEST>() {
			@Override
			public DEST call() throws Exception {
				if (browserScriptRunner.browser == null
						|| browserScriptRunner.browser.isDisposed()) {
					return null;
				}
				LOGGER.info("Running " + BrowserUtils.shortenScript(script));
				try {
					browserScriptRunner.scriptAboutToBeSentToBrowser(script);
					Object returnValue = browserScriptRunner.browser
							.evaluate(script);
					browserScriptRunner.scriptReturnValueReceived(returnValue);
					DEST rs = converter.convert(returnValue);
					LOGGER.info("Returned " + rs);
					return rs;
				} catch (SWTException e) {
					throw e;
				} catch (Exception e) {
					LOGGER.error(e);
					throw e;
				}
			}
		}, Browser.class, "Running " + script);
	}

	private final org.eclipse.swt.browser.Browser browser;
	private BrowserStatus browserStatus;

	private final OffWorker delayedScriptsWorker = new OffWorker(
			this.getClass(), "Script Runner");

	public BrowserScriptRunner(Browser browser) {
		Assert.isNotNull(browser);
		this.browser = browser;
		this.browserStatus = BrowserStatus.INITIALIZING;

		new BrowserFunction(browser, "error_callback") {
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
				LOGGER.error(e);
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
			if (browserStatus == BrowserStatus.CANCELLED) {
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
		case CANCELLED:
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
		case CANCELLED:
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
			this.runImmediately(
					"window.onerror = function(detail, filename, lineNumber) { if ( typeof window['error_callback'] !== 'function') return; return window['error_callback'](filename ? filename : 'unknown file', lineNumber ? lineNumber : 'unknown line number', detail ? detail : 'unknown detail'); }",
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
						new IConverter<Object, Boolean>() {
							@Override
							public Boolean convert(Object returnValue) {
								return true;
							}
						});
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
			return ExecUtils.nonUIAsyncExec(Browser.class,
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

							// runs the scripts that ends by calling the
							// callback
							// ...
							BrowserScriptRunner.this.run(js);
							try {
								// ... which destroys itself and releases this
								// lock
								mutex.acquire();
							} catch (InterruptedException e) {
								LOGGER.error(e);
							}
							return null;
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
			return this.delayedScriptsWorker.submit(new Callable<DEST>() {
				@Override
				public DEST call() throws Exception {
					switch (BrowserScriptRunner.this.browserStatus) {
					case LOADED:
						return ExecUtils.syncExec(scriptRunner);
					case CANCELLED:
						throw new ScriptExecutionException(script,
								new BrowserTimeoutException());
					default:
						throw new ScriptExecutionException(script,
								new UnexpectedBrowserStateException(
										BrowserScriptRunner.this.browserStatus
												.toString()));
					}
				}
			});
		case LOADED:
			try {
				return new CompletedFuture<DEST>(this.runImmediately(script,
						converter), null);
			} catch (Exception e) {
				return new CompletedFuture<DEST>(null, e);
			}
		case CANCELLED:
			return new CompletedFuture<DEST>(null,
					new ScriptExecutionException(script,
							new BrowserTimeoutException()));
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
	public void runImmediately(File script) throws Exception {
		String scriptContent = FileUtils.readFileToString(script);
		this.runImmediately(scriptContent, IConverter.CONVERTER_VOID);
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
	public void dispose() {
		if (this.delayedScriptsWorker != null
				&& !this.delayedScriptsWorker.isShutdown()) {
			this.delayedScriptsWorker.shutdown();
		}
	};
}
