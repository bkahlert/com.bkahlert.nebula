package com.bkahlert.devel.nebula.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.CompletedFuture;

/**
 * Offers functionality to run {@link Runnable}s and {@link Callable}
 * synchronously and asynchronously. In contrast to the functionality provided
 * by {@link Display} this util class allows also return values.
 * <p>
 * Code can be run in the following way:
 * <table>
 * <tr>
 * <th>&nbsp;</th>
 * <th>UI thread</th>
 * <th>Non-UI thread</th>
 * </tr>
 * <tr>
 * <th>Sync</th>
 * <td>{@link #syncExec(Callable)}<br>
 * {@link #syncExec(Runnable)}</td>
 * <td>{@link #nonUISyncExec(Callable)}<br>
 * {@link #nonUISyncExec(Runnable)}</td>
 * </tr>
 * <tr>
 * <th>Async</th>
 * <td>{@link #asyncExec(Callable)}<br>
 * {@link #asyncExec(Runnable)}</td>
 * <td>
 * {@link #nonUIAsyncExec(ExecutorService,Callable)}<br>
 * {@link #nonUIAsyncExec(ExecutorService,Runnable)}<br>
 * non-static {@link #nonUIAsyncExec(Callable)}<br>
 * non-static {@link #nonUIAsyncExec(Runnable)}</td>
 * </tr>
 * </table>
 * <p>
 * Synchronous non-UI thread code is executed by reusing an unused
 * {@link Thread} or if none are available, creating a new one.
 * <p>
 * Asynchronous non-UI thread code cannot freely create new {@link Thread}s.
 * This may run the code with a delay.
 * 
 * @author bkahlert
 * 
 */
public class ExecutorUtil {

	private static final Logger LOGGER = Logger.getLogger(ExecutorUtil.class);

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

		private final Runnable runnable;
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

	public interface ParametrizedCallable<T, V> {
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

	public static final java.util.concurrent.ExecutorService SYNC_POOL = Executors
			.newCachedThreadPool(new ThreadFactory() {
				private final ThreadFactory defaultThreadFactory = Executors
						.defaultThreadFactory();
				private int i = 0;

				@Override
				public Thread newThread(Runnable r) {
					Thread t = defaultThreadFactory.newThread(r);
					t.setName(ExecutorService.class.getSimpleName()
							+ "-POOL: #" + i);
					i++;
					return t;
				}
			});

	/**
	 * Checks if the current thread is the UI thread.
	 * 
	 * @return
	 */
	public static boolean isUIThread() {
		try {
			return Display.getDefault() == Display.getCurrent();
		} catch (SWTException e) {
			return false;
		}
	}

	/**
	 * Executes the given {@link Callable}.
	 * <p>
	 * Checks if the caller is already in the ui thread and if so runs the
	 * runnable directly in order to avoid deadlocks.
	 * 
	 * @param runnable
	 */
	public static <V> V syncExec(final Callable<V> callable) throws Exception {
		if (ExecutorUtil.isUIThread()) {
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
		if (ExecutorUtil.isUIThread()) {
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
		if (ExecutorUtil.isUIThread()) {
			V value = null;
			Exception exception = null;
			try {
				value = callable.call();
			} catch (Exception e) {
				exception = e;
			}
			return new CompletedFuture<V>(value, exception);
		}

		return SYNC_POOL.submit(new Callable<V>() {
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
		SYNC_POOL.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					LOGGER.error("Could not execute with a delay runnable "
							+ runnable);
				}

				Display.getDefault().asyncExec(runnable);
			}
		});
	}

	/**
	 * Runs the given {@link Callable} immediately in a non-UI thread. If the
	 * caller already runs in such one the {@link Callable} is simply executed.
	 * Otherwise a new thread is started.
	 * 
	 * @param callable
	 * @return
	 */
	public static <V> Future<V> nonUISyncExec(Callable<V> callable) {
		return SYNC_POOL.submit(callable);
	}

	/**
	 * Runs the given {@link Runnable} in a non-UI thread. If the caller already
	 * runs in such one the {@link Runnable} is simply executed. Otherwise a new
	 * thread is started.
	 * 
	 * @param runnable
	 */
	public static void nonUISyncExec(Runnable runnable) {
		if (ExecutorUtil.isUIThread()) {
			SYNC_POOL.execute(runnable);
		} else {
			runnable.run();
		}
	}

	/**
	 * Executes the given {@link Callable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @return
	 */
	public static <V> Future<V> nonUIAsyncExec(
			java.util.concurrent.ExecutorService executorService,
			final Callable<V> callable) {
		return executorService.submit(callable);
	}

	/**
	 * Executes the given {@link Callable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @param delay
	 * @return
	 */
	public static <V> Future<V> nonUIAsyncExec(
			java.util.concurrent.ExecutorService executorService,
			final Callable<V> callable, final int delay) {
		return nonUIAsyncExec(executorService, new Callable<V>() {
			@Override
			public V call() throws Exception {
				synchronized (this) {
					this.wait(delay);
					return callable.call();
				}
			}
		});
	}

	/**
	 * Executes the given {@link Runnable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @return
	 */
	public static Future<?> nonUIAsyncExec(
			java.util.concurrent.ExecutorService executorService,
			final Runnable runnable) {
		return executorService.submit(runnable);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @param delay
	 * @return
	 */
	public static Future<?> nonUIAsyncExec(
			java.util.concurrent.ExecutorService executorService,
			final Runnable runnable, final int delay) {
		return nonUIAsyncExec(executorService, new Runnable() {
			@Override
			public void run() {
				synchronized (this) {
					try {
						this.wait(delay);
						runnable.run();
					} catch (InterruptedException e) {
					}
				}
			}
		});
	}

	/**
	 * Executes the given {@link ExecutorUtil.ParametrizedCallable} once per
	 * element in the given input {@link Collection}.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecutorUtil.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a list of {@link Future}s
	 */
	public static <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			java.util.concurrent.ExecutorService executorService,
			Collection<INPUT> input,
			final ExecutorUtil.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		List<Future<OUTPUT>> futures = new ArrayList<Future<OUTPUT>>();
		for (Iterator<INPUT> iterator = input.iterator(); iterator.hasNext();) {
			final INPUT object = iterator.next();
			futures.add(executorService.submit(new Callable<OUTPUT>() {
				@Override
				public OUTPUT call() throws Exception {
					return parametrizedCallable.call(object);
				}
			}));
		}
		return futures;
	}

	/**
	 * Executes the given {@link ExecutorUtil.ParametrizedCallable} once per
	 * element in the given input {@link Collection}.
	 * <p>
	 * In contrast to
	 * {@link #nonUIAsyncExec(java.util.concurrent.ExecutorService, Collection, ParametrizedCallable)}
	 * this method returns a single {@link Future} containing all results.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecutorUtil.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a {@link Future} that contains the results
	 */
	public static <INPUT, OUTPUT> Iterable<OUTPUT> nonUIAsyncExecMerged(
			java.util.concurrent.ExecutorService executorService,
			Collection<INPUT> input,
			final ExecutorUtil.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		final List<Future<OUTPUT>> futures = new ArrayList<Future<OUTPUT>>();
		for (Iterator<INPUT> iterator = input.iterator(); iterator.hasNext();) {
			final INPUT object = iterator.next();
			futures.add(executorService.submit(new Callable<OUTPUT>() {
				@Override
				public OUTPUT call() throws Exception {
					return parametrizedCallable.call(object);
				}
			}));
		}
		return new Iterable<OUTPUT>() {
			@Override
			public Iterator<OUTPUT> iterator() {
				return new Iterator<OUTPUT>() {
					@Override
					public boolean hasNext() {
						return futures.size() > 0;
					}

					@Override
					public OUTPUT next() {
						if (!hasNext()) {
							return null;
						}

						return next(0);
					}

					public OUTPUT next(int numCalls) {
						if (numCalls == 200) {
							LOGGER.warn(ExecutorUtil.class
									+ " is busy waiting for the next available result since 10s. There might be a problem.");
						}

						Future<OUTPUT> next = null;
						for (Future<OUTPUT> future : futures) {
							if (future.isDone()) {
								next = future;
								break;
							}
						}
						if (next != null) {
							futures.remove(next);
							try {
								return next.get();
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							} catch (ExecutionException e) {
								throw new RuntimeException(e);
							}
						} else {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
							return next(numCalls + 1);
						}
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

	private final java.util.concurrent.ExecutorService asyncPool;

	/**
	 * Creates an instance of {@link ExecutorUtil} so the asynchronous non UI
	 * methods can easier be called.
	 * 
	 * @param executorService
	 *            to be used
	 */
	public ExecutorUtil(java.util.concurrent.ExecutorService executorService) {
		this.asyncPool = executorService;
	}

	/**
	 * Creates an instance of {@link ExecutorUtil} so the asynchronous non UI
	 * methods can easier be called.
	 * 
	 * @param clazz
	 *            this {@link ExecutorUtil} belongs to
	 */
	public ExecutorUtil(Class<?> clazz) {
		this(1l, clazz.getSimpleName());
	}

	/**
	 * Creates an instance of {@link ExecutorUtil} so the asynchronous non UI
	 * methods can easier be called.
	 * 
	 * @param nThreadsPerCore
	 *            number of threads per core available;<br>
	 *            e.g. <code>nThreadsPerCore = 2</code> and 4 cores leads to 8
	 *            {@link Thread}s
	 * @param name
	 *            to be used for the created threads. The following placeholders
	 *            do exist:
	 *            <dl>
	 *            <dt>@{i}</dt>
	 *            <dd>index of the current thread</dd>
	 *            <dt>@{max}</dt>
	 *            <dd>max number of threads</dd>
	 *            </dl>
	 */
	public ExecutorUtil(final long nThreadsPerCore, final String name) {
		this(
				(int) nThreadsPerCore
						* Runtime.getRuntime().availableProcessors(), name);
	}

	/**
	 * Creates an instance of {@link ExecutorUtil} so the asynchronous non UI
	 * methods can easier be called.
	 * 
	 * @param nThreads
	 *            number of threads available
	 * @param name
	 *            to be used for the created threads. The following placeholders
	 *            do exist:
	 *            <dl>
	 *            <dt>@{i}</dt>
	 *            <dd>index of the current thread</dd>
	 *            <dt>@{max}</dt>
	 *            <dd>max number of threads</dd>
	 *            </dl>
	 */
	public ExecutorUtil(final int nThreads, final String name) {
		this(Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			private final ThreadFactory defaultThreadFactory = Executors
					.defaultThreadFactory();
			private int i = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = defaultThreadFactory.newThread(r);
				t.setName(name.replace("@{i}", i + "").replace("@{max}",
						nThreads + ""));
				i++;
				return t;
			}
		}));
	}

	/**
	 * @see #nonUIAsyncExec(java.util.concurrent.ExecutorService, Callable)
	 */
	public <V> Future<V> nonUIAsyncExec(final Callable<V> callable) {
		return nonUIAsyncExec(asyncPool, callable);
	}

	/**
	 * @see #nonUIAsyncExec(java.util.concurrent.ExecutorService, Callable, int)
	 */
	public <V> Future<V> nonUIAsyncExec(final Callable<V> callable, int delay) {
		return nonUIAsyncExec(asyncPool, callable, delay);
	}

	/**
	 * @see #nonUIAsyncExec(java.util.concurrent.ExecutorService, Runnable)
	 */
	public Future<?> nonUIAsyncExec(final Runnable runnable) {
		return nonUIAsyncExec(asyncPool, runnable);
	}

	/**
	 * @see #nonUIAsyncExec(java.util.concurrent.ExecutorService, Runnable, int)
	 */
	public Future<?> nonUIAsyncExec(final Runnable runnable, int delay) {
		return nonUIAsyncExec(asyncPool, runnable, delay);
	}

	/**
	 * @see #nonUIAsyncExec(java.util.concurrent.ExecutorService, Collection,
	 *      ParametrizedCallable)
	 */
	public <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			Collection<INPUT> input,
			final ExecutorUtil.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(asyncPool, input, parametrizedCallable);
	}

	/**
	 * @see #nonUIAsyncExecMerged(java.util.concurrent.ExecutorService,
	 *      Collection, ParametrizedCallable)
	 */
	public <INPUT, OUTPUT> Iterable<OUTPUT> nonUIAsyncExecMerged(
			Collection<INPUT> input,
			final ExecutorUtil.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExecMerged(asyncPool, input, parametrizedCallable);
	}
}
