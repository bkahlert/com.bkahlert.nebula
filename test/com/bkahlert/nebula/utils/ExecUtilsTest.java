package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ExecUtils.DelayableThread;
import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

public class ExecUtilsTest {

	private static final int timePassedTolerance = 150;

	/**
	 * Tests if a {@link DelayableThread} correctly waits a specified delay
	 * before it runs the executable.
	 * 
	 * @param delay
	 *            to wait
	 * @param tolerance
	 * @return
	 * @throws InterruptedException
	 */
	public boolean testDelayableThreadSimple(long delay, long tolerance)
			throws InterruptedException {
		final AtomicLong finishedRunning = new AtomicLong(0l);
		DelayableThread dt = new DelayableThread(new Runnable() {
			@Override
			public void run() {
				finishedRunning.set(new Date().getTime());
			}
		}, delay);

		long startedRunning = new Date().getTime();
		dt.start();
		dt.join();
		long difference = finishedRunning.get() - startedRunning;
		return Math.abs(difference - delay) <= tolerance;
	}

	/**
	 * Tests if a {@link DelayableThread} correctly waits a specified delay
	 * before it runs the executable.
	 * 
	 * @param initialDelay
	 *            to wait
	 * @param timeToWaitBeforeSettingDelay
	 *            milliseconds after the delay is set to the new delay
	 * @param newDelay
	 *            that is set after a specified time has passed.
	 * @param tolerance
	 * @return
	 * @throws InterruptedException
	 */
	public boolean testDelayableThreadComplex(long initialDelay,
			final long timeToWaitBeforeSettingDelay, final long newDelay,
			long tolerance) throws InterruptedException {
		final AtomicLong finishedRunning = new AtomicLong(0l);
		final DelayableThread dt = new DelayableThread(new Runnable() {
			@Override
			public void run() {
				finishedRunning.set(new Date().getTime());
			}
		}, initialDelay);

		long startedRunning = new Date().getTime();
		dt.start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(timeToWaitBeforeSettingDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dt.setDelay(newDelay);
			}
		}).start();

		dt.join();
		long difference = finishedRunning.get() - startedRunning;
		long expected = initialDelay > timeToWaitBeforeSettingDelay ? timeToWaitBeforeSettingDelay
				+ newDelay
				: initialDelay;
		return Math.abs(difference - expected) <= tolerance;
	}

	@Test
	public void testDelayableThread() throws InterruptedException {
		assertTrue(this.testDelayableThreadSimple(0, 10));
		assertTrue(this.testDelayableThreadSimple(50, 10));
		assertTrue(this.testDelayableThreadSimple(200, 10));
		assertTrue(this.testDelayableThreadSimple(2000, 10));

		assertTrue(this.testDelayableThreadComplex(0, 1000, 2000, 10));
		assertTrue(this.testDelayableThreadComplex(1000, 2000, 1000000000, 10));

		assertTrue(this.testDelayableThreadComplex(50, 20, 100, 10));
		assertTrue(this.testDelayableThreadComplex(100, 90, 30, 120));
		assertTrue(this.testDelayableThreadComplex(2000, 1000, 2000, 10));
	}

	@Test
	public void testIsUIThread() throws Exception {
		assertFalse(ExecUtils.isUIThread());

		ExecUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				assertTrue(ExecUtils.isUIThread());
			}
		});

		ExecUtils.nonUISyncExec(new Runnable() {
			@Override
			public void run() {
				assertFalse(ExecUtils.isUIThread());
			}
		}).get();
	}

	/**
	 * Helper function that creates an UI thread and runs the testing callable
	 * once in the UI thread and once in a separate non-UI thread.
	 * 
	 * @param callable
	 */
	public void testInUIThreadAndNonUIThread(final Callable<Void> test)
			throws Exception {
		final Display display = Display.getDefault();
		final Shell shell = new Shell(display);

		// run in UI thread
		test.call();

		// run in non-UI thread
		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				test.call();
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						shell.dispose();
					}
				});
				return null;
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

	@Test
	public void testSyncExec() throws Exception {
		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				assertEquals(Display.getDefault(),
						ExecUtils.syncExec(new Callable<Display>() {
							@Override
							public Display call() throws Exception {
								return Display.getCurrent();
							}
						}));

				final AtomicReference<Display> rt = new AtomicReference<Display>();
				ExecUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						rt.set(Display.getCurrent());
					}
				});
				assertEquals(Display.getDefault(), rt.get());

				return null;
			}
		});
	}

	@Test
	public void testAsyncExec() throws Exception {
		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				assertEquals(Display.getDefault(),
						ExecUtils.asyncExec(new Callable<Display>() {
							@Override
							public Display call() throws Exception {
								return Display.getCurrent();
							}
						}).get());

				final AtomicReference<Display> rt = new AtomicReference<Display>();
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						rt.set(Display.getCurrent());
					}
				}).get();
				assertEquals(Display.getDefault(), rt.get());

				return null;
			}
		});
	}

	@Test
	public void testAsyncExecDelay() throws Exception {
		final int delay = 500;

		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final TimePassed cPassed = new TimePassed(true);
				final Future<Display> cReturn = ExecUtils.asyncExec(
						new Callable<Display>() {
							@Override
							public Display call() throws Exception {
								assertTrue(cPassed.getTimePassed()
										+ timePassedTolerance > delay);
								return Display.getCurrent();
							}
						}, delay);
				if (ExecUtils.isUIThread()) {
					ExecUtils.nonUIAsyncExec(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							// DIRTY, since JUnit propably has already finished
							// at this stage
							assertEquals(Display.getDefault(), cReturn.get());
							return null;
						}
					});
				} else {
					assertEquals(Display.getDefault(), cReturn.get());
				}

				final TimePassed rPassed = new TimePassed(true);
				final AtomicReference<Display> rt = new AtomicReference<Display>();
				final Future<Void> rReturn = ExecUtils.asyncExec(
						new Runnable() {
							@Override
							public void run() {
								assertTrue(rPassed.getTimePassed()
										+ timePassedTolerance > delay);
								rt.set(Display.getCurrent());
							}
						}, delay);
				if (ExecUtils.isUIThread()) {
					ExecUtils.nonUIAsyncExec(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							// DIRTY, since JUnit propably has already finished
							// at this stage
							rReturn.get();
							assertEquals(Display.getDefault(), rt.get());
							return null;
						}
					});
				} else {
					rReturn.get();
					assertEquals(Display.getDefault(), rt.get());
				}

				return null;
			}
		});
	}

	@Test
	public void testNonUISyncExec() throws Exception {
		final String test = "Hello World!";

		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				assertEquals(test,
						ExecUtils.nonUISyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								assertFalse(ExecUtils.isUIThread());
								return test;
							}
						}).get());

				final AtomicReference<String> rt = new AtomicReference<String>();
				ExecUtils.nonUISyncExec(new Runnable() {
					@Override
					public void run() {
						assertFalse(ExecUtils.isUIThread());
						rt.set(test);
					}
				}).get();
				assertEquals(test, rt.get());

				return null;
			}
		});
	}

	@Test
	public void testNonUISyncExecDelay() throws Exception {
		final String test = "Hello World!";
		final int delay = 500;

		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final TimePassed cPassed = new TimePassed(true);
				assertEquals(test,
						ExecUtils.nonUISyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								assertTrue(cPassed.getTimePassed()
										+ timePassedTolerance > delay);
								assertFalse(ExecUtils.isUIThread());
								return test;
							}
						}, delay).get());

				final TimePassed rPassed = new TimePassed(true);
				final AtomicReference<String> rt = new AtomicReference<String>();
				final Future<Void> rReturn = ExecUtils.nonUISyncExec(
						new Runnable() {
							@Override
							public void run() {
								assertTrue(rPassed.getTimePassed()
										+ timePassedTolerance > delay);
								assertFalse(ExecUtils.isUIThread());
								rt.set(test);
							}
						}, delay);
				rReturn.get();
				assertEquals(test, rt.get());
				assertTrue(rPassed.getTimePassed() + timePassedTolerance > delay);

				return null;
			}
		});
	}

	@Test
	public void testNonUIAsyncExec() throws Exception {
		final String test = "Hello World!";

		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Thread originThread = Thread.currentThread();

				assertEquals(test,
						ExecUtils.nonUIAsyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								assertFalse(ExecUtils.isUIThread());
								assertFalse(originThread == Thread
										.currentThread());
								return test;
							}
						}).get());

				final AtomicReference<String> rt = new AtomicReference<String>();
				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						assertFalse(ExecUtils.isUIThread());
						assertFalse(originThread == Thread.currentThread());
						rt.set(test);
					}
				}).get();
				assertEquals(test, rt.get());

				return null;
			}
		});
	}

	@Test
	public void testNonUIAsyncExecDelay() throws Exception {
		final String test = "Hello World!";
		final int delay = 500;

		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Thread originThread = Thread.currentThread();

				final TimePassed cPassed = new TimePassed(true);
				assertEquals(test,
						ExecUtils.nonUIAsyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								assertTrue(cPassed.getTimePassed()
										+ timePassedTolerance > delay);
								assertFalse(ExecUtils.isUIThread());
								assertFalse(originThread == Thread
										.currentThread());
								return test;
							}
						}, delay).get());

				final TimePassed rPassed = new TimePassed(true);
				final AtomicReference<String> rt = new AtomicReference<String>();
				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						assertTrue(rPassed.getTimePassed()
								+ timePassedTolerance > delay);
						assertFalse(ExecUtils.isUIThread());
						assertFalse(originThread == Thread.currentThread());
						rt.set(test);
					}
				}, delay).get();
				assertEquals(test, rt.get());

				return null;
			}
		});
	}

	@Test
	public void testNonUIAsyncExecBatch() throws Exception {
		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				List<Future<String>> rt = ExecUtils.nonUIAsyncExec(
						ExecUtilsTest.class, "testNonUIAsyncExecBatch",
						new LinkedList<Integer>(Arrays.asList(1500, 50, 500)),
						new ExecUtils.ParametrizedCallable<Integer, String>() {
							@Override
							public String call(Integer sleepTime)
									throws Exception {
								Thread.sleep(sleepTime);
								return "--" + sleepTime + "--";
							}
						});
				List<String> expected = new ArrayList<String>(Arrays.asList(
						"--50--", "--500--", "--1500--"));
				for (Future<String> future : rt) {
					String expected_ = future.get();
					assertTrue(expected.contains(expected_));
					expected.remove(expected_);
				}
				assertEquals(0, expected.size());

				return null;
			}
		});
	}

	@Test
	public void testNonUIAsyncExecMerged() throws Exception {
		this.testInUIThreadAndNonUIThread(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Iterable<String> rt = ExecUtils.nonUIAsyncExecMerged(
						ExecUtilsTest.class, "testNonUIAsyncExecMerged",
						new LinkedList<Integer>(Arrays.asList(1500, 50, 500)),
						new ExecUtils.ParametrizedCallable<Integer, String>() {
							@Override
							public String call(Integer sleepTime)
									throws Exception {
								Thread.sleep(sleepTime);
								return "--" + sleepTime + "--";
							}
						});
				Iterator<String> iterator = rt.iterator();
				assertEquals(true, iterator.hasNext());
				assertEquals("--50--", iterator.next());
				assertEquals(true, iterator.hasNext());
				assertEquals("--500--", iterator.next());
				assertEquals(true, iterator.hasNext());
				assertEquals("--1500--", iterator.next());
				assertEquals(false, iterator.hasNext());
				assertEquals(null, iterator.next());

				return null;
			}
		});
	}
}
