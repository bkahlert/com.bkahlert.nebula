package com.bkahlert.devel.nebula.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

public class ExecutorUtil {

	private static final Logger LOGGER = Logger.getLogger(ExecutorUtil.class);
	private static final ExecutorService asyncExecPool = ExecutorUtil
			.newFixedMultipleOfProcessorsThreadPool(4);

	public static interface ParametrizedCallable<T, V> {
		/**
		 * Computes a result, or throws an exception if unable to do so.
		 * 
		 * @param given
		 *            value
		 * @return computed result
		 * @throws Exception
		 *             if unable to compute a result
		 */
		public V call(T object) throws Exception;
	}

	/**
	 * Threads whose execution is delayed relating to the moment
	 * {@link DelayableThread#start()} is called.
	 * <p>
	 * Example:
	 * <ol>
	 * <li>You instantiated the {@link DelayableThread} at 00:00:00 with delay
	 * 5000 milliseconds.</li>
	 * <li>At 00:00:20 you started your thread.</li>
	 * <li>The passed runnable with be called at 00:00:25.</li>
	 * </ol>
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class DelayableThread extends Thread {

		private Runnable runnable;
		private long delay;
		private boolean run;

		/**
		 * Creates an {@link DelayableThread} that is delayed by the specified
		 * delay.
		 * 
		 * @param runnable
		 *            to run when after {@link DelayableThread#start()} has been
		 *            called and the specified delay has passed
		 * @param delay
		 *            in milliseconds
		 */
		public DelayableThread(Runnable runnable, long delay) {
			super(DelayableThread.class.getSimpleName());
			this.runnable = runnable;
			this.delay = delay;
			this.run = false;
		}

		@Override
		public void run() {
			if (!this.run) {
				try {
					Thread.sleep(this.delay);
					this.runnable.run();
					this.run = true;
				} catch (InterruptedException e) {
					this.run();
				}
			} else {
				LOGGER.fatal(DelayableThread.class.getSimpleName()
						+ " was called more than once. This is an implementation error.");
			}
		}

		/**
		 * Sets the new delay. Makes the thread forget the old delay and lets it
		 * wait for the new delay.
		 * <p>
		 * Example:
		 * <ol>
		 * <li>You instantiated the {@link DelayableThread} at 00:00:00 with
		 * delay 5000 milliseconds.</li>
		 * <li>At 00:00:20 you started your thread.</li>
		 * <li>At 00:00:22 you called {@link DelayableThread#setDelay(long)}
		 * with 10.000 milliseconds (= 10 seconds).</li>
		 * <li>The passed runnable with be called at 00:00:32.</li>
		 * </ol>
		 * <p>
		 * Calling this method after the runnable was executed has no effect.
		 * 
		 * @param delay
		 *            in milliseconds.
		 */
		public void setDelay(long delay) {
			this.delay = delay;
			this.interrupt();
		}

		/**
		 * Returns true if this {@link DelayableThread} was already executed.
		 * 
		 * @return
		 */
		public boolean isFinished() {
			return this.run;
		}
	}

	/**
	 * Creates a thread pool that reuses a fixed number of threads operating off
	 * a shared unbounded queue. At any point, at most a multiple times of the
	 * available processors count threads will be active processing tasks.
	 * 
	 * @param multiple
	 * @return
	 * 
	 * @see Executors#newFixedThreadPool(int)
	 */
	public static ExecutorService newFixedMultipleOfProcessorsThreadPool(
			int multiple) {
		int numProcessors = Runtime.getRuntime().availableProcessors();
		return Executors.newFixedThreadPool(multiple * numProcessors);
	}

	/**
	 * Executes the given {@link Callable} in the UI thread.
	 * <p>
	 * The return value is returned back in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * @throws Exception
	 */
	public static <V> V syncExec(final Callable<V> callable) throws Exception {
		if (isUIThread()) {
			return callable.call();
		}

		final AtomicReference<V> r = new AtomicReference<V>();
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		final Semaphore mutex = new Semaphore(0);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					r.set(callable.call());
				} catch (Exception e) {
					exception.set(e);
				}
				mutex.release();
			}
		});
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
		if (exception.get() != null) {
			throw exception.get();
		}
		return r.get();
	}

	/**
	 * Executes the given {@link Runnable}.
	 * <p>
	 * Checks if the caller is already in the ui thread and if so runs the
	 * runnable directly in order to avoid deadlocks.
	 * 
	 * @param runnable
	 */
	public static void syncExec(Runnable runnable) {
		if (isUIThread()) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	/**
	 * Executes the given {@link Callable} asynchronously in the UI thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 */
	public static <V> Future<V> asyncExec(final Callable<V> callable) {
		return asyncExecPool.submit(new Callable<V>() {
			@Override
			public V call() throws Exception {
				final AtomicReference<V> r = new AtomicReference<V>();
				final AtomicReference<Exception> exception = new AtomicReference<Exception>();
				final Semaphore mutex = new Semaphore(0);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							r.set(callable.call());
						} catch (Exception e) {
							exception.set(e);
						}
						mutex.release();
					}
				});
				mutex.acquire();
				if (exception.get() != null) {
					throw exception.get();
				}
				return r.get();
			}
		});
	}

	/**
	 * Executes the given {@link Runnable} asynchronously in the UI thread.
	 * 
	 * @param runnable
	 */
	public static void asyncExec(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}

	/**
	 * Executes the given {@link Runnable} delayedly and asynchronously in the
	 * UI thread.
	 * 
	 * @param runnable
	 * @param delay
	 */
	public static void asyncExec(final Runnable runnable, final long delay) {
		ExecutorUtil.asyncRun(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					LOGGER.error("Could not delayedly execute runnable "
							+ runnable);
				}

				Display.getDefault().asyncExec(runnable);
			}
		});
	}

	/**
	 * Runs the given {@link Runnable} in a non-UI thread. If the caller already
	 * runs in such one the {@link Runnable} is simply executed. Otherwise a new
	 * thread is started.
	 * 
	 * @param runnable
	 */
	public static void nonUIExec(Runnable runnable) {
		if (isUIThread()) {
			new Thread(runnable).start();
		} else {
			runnable.run();
		}
	}

	/**
	 * Executes the given {@link Callable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 */
	public static <V> Future<V> nonUIAsyncExec(final Callable<V> callable) {
		return asyncExecPool.submit(callable);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 */
	public static void nonUIAsyncExec(final Runnable runnable) {
		asyncExecPool.submit(runnable);
	}

	public static <T, V> List<Future<V>> nonUIAsyncExec(
			Collection<T> collection,
			final ParametrizedCallable<T, V> parametrizedCallable) {
		return nonUIAsyncExec(asyncExecPool, collection, parametrizedCallable);
	}

	public static <T, V> List<Future<V>> nonUIAsyncExec(
			ExecutorService executorService, Collection<T> collection,
			final ParametrizedCallable<T, V> parametrizedCallable) {
		List<Future<V>> futures = new ArrayList<Future<V>>();
		for (Iterator<T> iterator = collection.iterator(); iterator.hasNext();) {
			final T object = iterator.next();
			futures.add(executorService.submit(new Callable<V>() {
				@Override
				public V call() throws Exception {
					return parametrizedCallable.call(object);
				}
			}));
		}
		return futures;
	}

	/**
	 * Checks if the current thread is the UI thread.
	 * 
	 * @return
	 */
	public static boolean isUIThread() {
		return Display.getCurrent() == Display.getDefault();
	}

	/**
	 * Runs the runnable in a separate {@link Thread} and returns it.
	 * 
	 * @param
	 * @return
	 */
	public static Thread asyncRun(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.start();
		return thread;
	}

	/**
	 * Runs the runnable delayedly in a separate {@link Thread} and returns it.
	 * 
	 * @param
	 * @return the thread that is delayedly executed
	 */
	public static void asyncRun(final Runnable runnable, final long delay) {
		ExecutorUtil.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					LOGGER.error("Could not delayedly execute runnable "
							+ runnable);
				}

				ExecutorUtil.asyncExec(runnable);
			}
		});
	}

	private ExecutorUtil() {
	}
}
