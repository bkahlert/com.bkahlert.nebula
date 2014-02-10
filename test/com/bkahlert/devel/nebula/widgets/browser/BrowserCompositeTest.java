package com.bkahlert.devel.nebula.widgets.browser;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.utils.IConverter;

public class BrowserCompositeTest {

	@Test
	public void testJavaScriptRunOrder() throws InterruptedException,
			ExecutionException {

		final int numRuns = 200;
		final int numThreads = 5;

		Display display = new Display();
		final Shell shell = new Shell(display);

		shell.setLayout(new FillLayout());

		final List<String> scriptSubmitOrder = Collections
				.synchronizedList(new ArrayList<String>());
		final List<String> scriptExecutionOrder = Collections
				.synchronizedList(new ArrayList<String>());
		final Map<String, String> scriptResults = Collections
				.synchronizedMap(new HashMap<String, String>());
		final List<String> resultFinishedOrder = Collections
				.synchronizedList(new ArrayList<String>());
		final BrowserComposite browserComposite = new BrowserComposite(shell,
				SWT.NONE) {
			@Override
			public void scriptAboutToBeSentToBrowser(String script) {
				if (!script.contains("successfullyInjectedAnkerHoverCallback")) {
					scriptExecutionOrder.add(script);
				}
			}

			@Override
			public void scriptReturnValueReceived(Object returnValue) {
				if (!returnValue.toString().equals("true")
						&& !returnValue.toString().equals("false")) {
					resultFinishedOrder.add(returnValue.toString());
				}
			}
		};

		shell.setSize(800, 600);
		shell.open();

		browserComposite.setAllowLocationChange(true);
		browserComposite.open("http://bkahlert.com", 5000);

		final Thread[] threads = new Thread[numThreads];
		for (int thread = 0; thread < threads.length; thread++) {
			threads[thread] = new Thread(new Runnable() {
				@Override
				public void run() {
					List<Future<String>> finish = new ArrayList<Future<String>>();
					for (int run = 0; run < numRuns; run++) {
						final String random = new BigInteger(130,
								new SecureRandom()).toString(32);
						String script = "document.write(\"" + random
								+ "<br>\");return \"" + random + "\";";

						if (run > numRuns / 2) {
							// run the other half when browser is loaded
							while (!browserComposite.isLoadingCompleted()) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

						synchronized (BrowserCompositeTest.class) {
							scriptResults.put(random, script);
							scriptSubmitOrder.add(script);
							finish.add(browserComposite.run(script,
									IConverter.CONVERTER_STRING));
						}
					}
					for (Future<String> f : finish) {
						try {
							f.get();
						} catch (Exception e) {
							Assert.assertTrue(false);
						}
					}
				}

			});
			threads[thread].start();
		}

		final Future<?> assertionJoin = ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				for (Thread thread : threads) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				for (int i = 0, m = numRuns * numThreads; i < m; i++) {
					String submitted = scriptSubmitOrder.get(i);
					String executed = scriptExecutionOrder.get(i);

					Assert.assertEquals(submitted, executed);

					String returnValue = resultFinishedOrder.get(i);
					String finished = scriptResults.get(returnValue);

					Assert.assertEquals(executed, finished);
				}

				System.err.println("exit");
			}
		});

		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					assertionJoin.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						shell.close();
					}
				});
			}
		});

		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				// If no more entries in event queue
				display.sleep();
			}
		}
	}
}
