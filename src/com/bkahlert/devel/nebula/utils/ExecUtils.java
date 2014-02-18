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
 * {@link #nonUIAsyncExec(Callable)}<br>
 * {@link #nonUIAsyncExec(Runnable)}</td>
 * </tr>
 * </table>
 * <p>
 * Synchronous non-UI thread code is executed immediately. They still return a
 * {@link Future} instead of the value itself. Should the call have been made
 * from an UI thread the code must be executed in a separate thread.
 * <p>
 * Asynchronous non-UI thread naturally always returns a {@link Future}.
 * <p>
 * To allow asynchronous non-UI thread methods to work with a custom
 * {@link ExecutorService}, just instantiate {@link ExecUtils}. This is
 * especially practical if you want to limit the number of maximum threads.
 * 
 * @author bkahlert
 * 
 */
public class ExecUtils {

	private static final Logger LOGGER = Logger.getLogger(ExecUtils.class);

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

	private static final ExecutorService EXECUTOR_SERVICE = Executors
			.newCachedThreadPool(new ThreadFactory() {
				private final ThreadFactory defaultThreadFactory = Executors
						.defaultThreadFactory();
				private int i = 0;

				@Override
				public Thread newThread(Runnable r) {
					Thread t = this.defaultThreadFactory.newThread(r);
					t.setName(ExecUtils.class.getSimpleName() + " #" + this.i);
					this.i++;
					return t;
				}
			});

	/**
	 * Checks if the current thread is the/an UI thread.
	 * 
	 * @return
	 */
	public static boolean isUIThread() {
		try {
			return Display.getCurrent() != null;
		} catch (SWTException e) {
			return false;
		}
	}

	private static ThreadLocal<String> threadLabelBackup = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return null;
		};
	};

	public static String backupThreadLabel() {
		String label = Thread.currentThread().getName();
		threadLabelBackup.set(label);
		return label;
	}

	public static void restoreThreadLabel() {
		String label = threadLabelBackup.get();
		if (label != null) {
			Thread.currentThread().setName(label);
		}
	}

	public static void setThreadLabel(Class<?> clazz, String purpose) {
		setThreadLabel("", clazz, purpose);
	}

	public static void setThreadLabel(String prefix, Class<?> clazz,
			String purpose) {
		Thread.currentThread().setName(
				prefix + clazz.getSimpleName() + " :: " + purpose);
	}

	public static <I, O> ParametrizedCallable<I, O> createThreadLabelingCode(
			final ParametrizedCallable<I, O> parametrizedCallable,
			final Class<?> clazz, final String purpose) {
		return new ParametrizedCallable<I, O>() {
			@Override
			public O call(I i) throws Exception {
				String oldName = Thread.currentThread().getName();
				setThreadLabel(oldName + " :: ", clazz, purpose);
				try {
					return parametrizedCallable.call(i);
				} finally {
					Thread.currentThread().setName(oldName);
				}
			}
		};
	}

	public static <V> Callable<V> createThreadLabelingCode(
			final Callable<V> callable, final Class<?> clazz,
			final String purpose) {
		return new Callable<V>() {
			@Override
			public V call() throws Exception {
				String oldName = Thread.currentThread().getName();
				setThreadLabel(oldName + " :: ", clazz, purpose);
				try {
					return callable.call();
				} finally {
					Thread.currentThread().setName(oldName);
				}
			}
		};
	}

	public static Runnable createThreadLabelingCode(final Runnable runnable,
			final Class<?> clazz, final String purpose) {
		return new Runnable() {
			@Override
			public void run() {
				String oldName = Thread.currentThread().getName();
				setThreadLabel(oldName + " :: ", clazz, purpose);
				try {
					runnable.run();
				} finally {
					Thread.currentThread().setName(oldName);
				}
			}
		};
	}

	/**
	 * Executes the given {@link Callable}.
	 * <p>
	 * Checks if the caller is already in the UI thread and if so runs the
	 * runnable directly in order to avoid deadlocks.
	 * 
	 * @param runnable
	 */
	public static <V> V syncExec(final Callable<V> callable) throws Exception {
		if (ExecUtils.isUIThread()) {
			return callable.call();
		}

		final AtomicReference<V> r = new AtomicReference<V>();
		final AtomicReference<Exception> exception = new AtomicReference<Exception>();
		final Semaphore mutex = new Semaphore(0);
		Display.getDefault().syncExec(new Runnable() {
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
	 * Checks if the caller is already in the UI thread and if so runs the
	 * runnable directly in order to avoid deadlocks.
	 * 
	 * @param runnable
	 * @throws Exception
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static void syncExec(final Runnable runnable) throws Exception {
		if (ExecUtils.isUIThread()) {
			runnable.run();
		} else {
			final AtomicReference<Exception> exception = new AtomicReference<Exception>();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						runnable.run();
					} catch (Exception e) {
						exception.set(e);
					}
				}
			});
			if (exception.get() != null) {
				throw exception.get();
			}
		}
	}

	/**
	 * Executes the given {@link Callable} asynchronously in the UI thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> asyncExec(final Callable<V> callable) {
		if (ExecUtils.isUIThread()) {
			return new CompletedFuture<V>(callable);
		}

		return EXECUTOR_SERVICE.submit(new Callable<V>() {
			@Override
			public V call() throws Exception {
				return syncExec(callable);
			}
		});
	}

	/**
	 * Executes the given {@link Runnable} asynchronously in the UI thread.
	 * 
	 * @param runnable
	 * @return can be used to check when the code has been executed
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<Void> asyncExec(final Runnable runnable) {
		if (ExecUtils.isUIThread()) {
			return new CompletedFuture<Void>(runnable);
		}

		return EXECUTOR_SERVICE.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final AtomicReference<Exception> exception = new AtomicReference<Exception>();
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						try {
							runnable.run();
						} catch (Exception e) {
							exception.set(e);
						}
					}
				});
				if (exception.get() != null) {
					throw exception.get();
				}
				return null;
			}
		});
	}

	/**
	 * Executes the given {@link Callable} with a delay and asynchronously in
	 * the UI thread.
	 * 
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread <b><span style="color: #f00;">WARNING: </span>Do not use the
	 *           returned {@link Future} to check the finished execution. This
	 *           results in a deadlock if the delay has not been passed but the
	 *           checking UI thread block the execution.</b>
	 * @NonUIThread
	 * 
	 *              TODO implement using Display.timerExec
	 */
	public static <V> Future<V> asyncExec(final Callable<V> callable,
			final long delay) {
		return EXECUTOR_SERVICE.submit(new Callable<V>() {
			@Override
			public V call() throws Exception {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					LOGGER.error("Could not execute with a delay callable "
							+ callable);
				}

				return syncExec(callable);
			}
		});
	}

	/**
	 * Executes the given {@link Runnable} with a delay and asynchronously in
	 * the UI thread.
	 * 
	 * @param runnable
	 * @param delay
	 * 
	 * @UIThread <b><span style="color: #f00;">WARNING: </span>Do not use the
	 *           returned {@link Future} to check the finished execution. This
	 *           results in a deadlock if the delay has not been passed but the
	 *           checking UI thread block the execution.</b>
	 * @NonUIThread
	 * 
	 *              TODO implement using Display.timerExec
	 */
	public static Future<Void> asyncExec(final Runnable runnable,
			final long delay) {
		return EXECUTOR_SERVICE.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					LOGGER.error("Could not execute with a delay runnable "
							+ runnable);
				}

				syncExec(runnable);
				return null;
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
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUISyncExec(Callable<V> callable) {
		if (ExecUtils.isUIThread()) {
			return EXECUTOR_SERVICE.submit(callable);
		} else {
			return new CompletedFuture<V>(callable);
		}
	}

	/**
	 * Runs the given {@link Runnable} immediately in a non-UI thread. If the
	 * caller already runs in such one the {@link Runnable} is simply executed.
	 * Otherwise a new thread is started.
	 * 
	 * @param runnable
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<Void> nonUISyncExec(final Runnable runnable) {
		if (ExecUtils.isUIThread()) {
			return EXECUTOR_SERVICE.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					runnable.run();
					return null;
				}
			});
		} else {
			return new CompletedFuture<Void>(runnable);
		}
	}

	/**
	 * Runs the given {@link Callable} immediately in a non-UI thread. If the
	 * caller already runs in such one the {@link Callable} is simply executed.
	 * Otherwise a new thread is started.
	 * <p>
	 * The given {@link Class} and purpose are used to give the thread a
	 * reasonable name.
	 * 
	 * @param clazz
	 *            that invokes this call
	 * @param purpose
	 *            of the callable
	 * @param callable
	 * 
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUISyncExec(final Class<?> clazz,
			final String purpose, Callable<V> callable) {
		return nonUISyncExec(createThreadLabelingCode(callable, clazz, purpose));
	}

	/**
	 * Runs the given {@link Runnable} immediately in a non-UI thread. If the
	 * caller already runs in such one the {@link Runnable} is simply executed.
	 * Otherwise a new thread is started.
	 * <p>
	 * The given {@link Class} and purpose are used to give the thread a
	 * reasonable name.
	 * 
	 * @param clazz
	 *            that invokes this call
	 * @param purpose
	 *            of the runnable
	 * @param runnable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<?> nonUISyncExec(final Class<?> clazz,
			final String purpose, Runnable runnable) {
		return nonUISyncExec(createThreadLabelingCode(runnable, clazz, purpose));
	}

	/**
	 * Executes the given {@link Callable} synchronously with a delay.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUISyncExec(final Callable<V> callable,
			final int delay) {
		return nonUIAsyncExec(callable, delay);
	}

	/**
	 * Executes the given {@link Runnable} synchronously with a delay.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<Void> nonUISyncExec(final Runnable runnable,
			final int delay) {
		return nonUIAsyncExec(runnable, delay);
	}

	/**
	 * Runs the given {@link Callable} with a delay in a non-UI thread. If the
	 * caller already runs in such one the {@link Callable} is simply executed.
	 * Otherwise a new thread is started.
	 * <p>
	 * The given {@link Class} and purpose are used to give the thread a
	 * reasonable name.
	 * 
	 * @param clazz
	 *            that invokes this call
	 * @param purpose
	 *            of the callable
	 * @param callable
	 * @param delay
	 * 
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUISyncExec(final Class<?> clazz,
			final String purpose, Callable<V> callable, int delay) {
		return nonUISyncExec(
				createThreadLabelingCode(callable, clazz, purpose), delay);
	}

	/**
	 * Runs the given {@link Runnable} with a delay in a non-UI thread. If the
	 * caller already runs in such one the {@link Runnable} is simply executed.
	 * Otherwise a new thread is started.
	 * <p>
	 * The given {@link Class} and purpose are used to give the thread a
	 * reasonable name.
	 * 
	 * @param clazz
	 *            that invokes this call
	 * @param purpose
	 *            of the runnable
	 * @param runnable
	 * @param delay
	 * 
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<?> nonUISyncExec(final Class<?> clazz,
			final String purpose, Runnable runnable, int delay) {
		return nonUISyncExec(
				createThreadLabelingCode(runnable, clazz, purpose), delay);
	}

	/**
	 * Executes the given {@link Callable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUIAsyncExec(final Callable<V> callable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, callable);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<Void> nonUIAsyncExec(final Runnable runnable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, runnable);
	}

	public static <V> Future<V> nonUIAsyncExec(final Class<?> clazz,
			final String purpose, final Callable<V> callable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, callable);
	}

	public static Future<Void> nonUIAsyncExec(final Class<?> clazz,
			final String purpose, final Runnable runnable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, runnable);
	}

	/**
	 * Executes the given {@link Callable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <V> Future<V> nonUIAsyncExec(final Callable<V> callable,
			final int delay) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, callable, delay);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static Future<Void> nonUIAsyncExec(final Runnable runnable,
			final int delay) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, runnable, delay);
	}

	public static <V> Future<V> nonUIAsyncExec(final Class<?> clazz,
			final String purpose, final Callable<V> callable, final int delay) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, callable, delay);
	}

	public static Future<Void> nonUIAsyncExec(final Class<?> clazz,
			final String purpose, final Runnable runnable, final int delay) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, runnable, delay);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection} and each in a new thread.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a list of {@link Future}s
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, input, parametrizedCallable);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection} and each in a new thread.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a list of {@link Future}s
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			Class<?> clazz,
			String purpose,
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, input,
				parametrizedCallable);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection}.
	 * <p>
	 * In contrast to
	 * {@link #nonUIAsyncExec(java.util.concurrent.ExecutorService, Collection, ParametrizedCallable)}
	 * this method returns a single {@link Future} containing all results.
	 * 
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a {@link Future} that contains the results
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public static <INPUT, OUTPUT> Iterable<OUTPUT> nonUIAsyncExecMerged(
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExecMerged(EXECUTOR_SERVICE, input,
				parametrizedCallable);
	}

	private static <V> Future<V> nonUIAsyncExec(
			ExecutorService executorService, final Callable<V> callable) {
		return EXECUTOR_SERVICE.submit(callable);
	}

	private static Future<Void> nonUIAsyncExec(ExecutorService executorService,
			final Runnable runnable) {
		return EXECUTOR_SERVICE.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				runnable.run();
				return null;
			}
		});
	}

	private static <V> Future<V> nonUIAsyncExec(
			ExecutorService executorService, final Class<?> clazz,
			final String purpose, final Callable<V> callable) {
		return nonUIAsyncExec(executorService,
				createThreadLabelingCode(callable, clazz, purpose));
	}

	private static Future<Void> nonUIAsyncExec(ExecutorService executorService,
			final Class<?> clazz, final String purpose, final Runnable runnable) {
		return nonUIAsyncExec(executorService,
				createThreadLabelingCode(runnable, clazz, purpose));
	}

	private static <V> Future<V> nonUIAsyncExec(
			ExecutorService executorService, final Callable<V> callable,
			final int delay) {
		return nonUIAsyncExec(new Callable<V>() {
			@Override
			public V call() throws Exception {
				synchronized (this) {
					this.wait(delay);
					return callable.call();
				}
			}
		});
	}

	private static Future<Void> nonUIAsyncExec(ExecutorService executorService,
			final Runnable runnable, final int delay) {
		return nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				synchronized (this) {
					try {
						this.wait(delay);
						runnable.run();
					} catch (InterruptedException e) {
					}
				}
				return null;
			}
		});
	}

	private static <V> Future<V> nonUIAsyncExec(
			ExecutorService executorService, final Class<?> clazz,
			final String purpose, final Callable<V> callable, final int delay) {
		return nonUIAsyncExec(executorService,
				createThreadLabelingCode(callable, clazz, purpose), delay);
	}

	private static Future<Void> nonUIAsyncExec(ExecutorService executorService,
			final Class<?> clazz, final String purpose,
			final Runnable runnable, final int delay) {
		return nonUIAsyncExec(executorService,
				createThreadLabelingCode(runnable, clazz, purpose), delay);
	}

	private static <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			ExecutorService executorService,
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		List<Future<OUTPUT>> futures = new ArrayList<Future<OUTPUT>>();
		for (Iterator<INPUT> iterator = input.iterator(); iterator.hasNext();) {
			final INPUT object = iterator.next();
			futures.add(EXECUTOR_SERVICE.submit(new Callable<OUTPUT>() {
				@Override
				public OUTPUT call() throws Exception {
					return parametrizedCallable.call(object);
				}
			}));
		}
		return futures;
	}

	private static <INPUT, OUTPUT> List<Future<OUTPUT>> nonUIAsyncExec(
			ExecutorService executorService,
			Class<?> clazz,
			String purpose,
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(executorService, input,
				createThreadLabelingCode(parametrizedCallable, clazz, purpose));
	}

	private static <INPUT, OUTPUT> Iterable<OUTPUT> nonUIAsyncExecMerged(
			ExecutorService executorService,
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		final List<Future<OUTPUT>> futures = new ArrayList<Future<OUTPUT>>();
		for (Iterator<INPUT> iterator = input.iterator(); iterator.hasNext();) {
			final INPUT object = iterator.next();
			futures.add(EXECUTOR_SERVICE.submit(new Callable<OUTPUT>() {
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
						if (!this.hasNext()) {
							return null;
						}

						return this.next(0);
					}

					public OUTPUT next(int numCalls) {
						if (numCalls == 200) {
							LOGGER.warn(ExecUtils.class
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
							return this.next(numCalls + 1);
						}
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}

	private final java.util.concurrent.ExecutorService customExecutorService;

	/**
	 * Creates an instance of {@link ExecUtils} so that the asynchronous non UI
	 * methods can also use a custom {@link ExecutorService}.
	 * 
	 * @param customExecutorService
	 *            to be used
	 */
	public ExecUtils(java.util.concurrent.ExecutorService customExecutorService) {
		this.customExecutorService = customExecutorService;
	}

	/**
	 * Creates an instance of {@link ExecUtils} so that the asynchronous non UI
	 * methods can also use a custom {@link ExecutorService}.
	 * 
	 * @param clazz
	 *            this {@link ExecUtils} belongs to
	 */
	public ExecUtils(Class<?> clazz) {
		this(1l, clazz.getSimpleName());
	}

	/**
	 * Creates an instance of {@link ExecUtils} so that the asynchronous non UI
	 * methods can also use a custom {@link ExecutorService}.
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
	public ExecUtils(final long nThreadsPerCore, final String name) {
		this(
				(int) nThreadsPerCore
						* Runtime.getRuntime().availableProcessors(), name);
	}

	/**
	 * Creates an instance of {@link ExecUtils} so that the asynchronous non UI
	 * methods can also use a custom {@link ExecutorService}.
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
	public ExecUtils(final int nThreads, final String name) {
		this(Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			private final ThreadFactory defaultThreadFactory = Executors
					.defaultThreadFactory();
			private int i = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread t = this.defaultThreadFactory.newThread(r);
				t.setName(name.replace("@{i}", this.i + "").replace("@{max}",
						nThreads + ""));
				this.i++;
				return t;
			}
		}));
	}

	/**
	 * Executes the given {@link Callable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public <V> Future<V> customNonUIAsyncExec(final Callable<V> callable) {
		return nonUIAsyncExec(this.customExecutorService, callable);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public Future<Void> customNonUIAsyncExec(final Runnable runnable) {
		return nonUIAsyncExec(this.customExecutorService, runnable);
	}

	public <V> Future<V> customNonUIAsyncExec(Class<?> clazz, String purpose,
			final Callable<V> callable) {
		return nonUIAsyncExec(this.customExecutorService, clazz, purpose,
				callable);
	}

	public Future<Void> customNonUIAsyncExec(Class<?> clazz, String purpose,
			final Runnable runnable) {
		return nonUIAsyncExec(this.customExecutorService, clazz, purpose,
				runnable);
	}

	/**
	 * Executes the given {@link Callable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public <V> Future<V> customNonUIAsyncExec(final Callable<V> callable,
			final int delay) {
		return nonUIAsyncExec(this.customExecutorService, callable, delay);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously, meaning always in a
	 * new thread.
	 * <p>
	 * The return value is returned in the calling thread.
	 * 
	 * @param executorService
	 *            to be used to get the {@link Thread} in which to run the code
	 * @param callable
	 * @param delay
	 * @return
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public Future<Void> customNonUIAsyncExec(final Runnable runnable,
			final int delay) {
		return nonUIAsyncExec(this.customExecutorService, runnable, delay);
	}

	public <V> Future<V> customNonUIAsyncExec(Class<?> clazz, String purpose,
			final Callable<V> callable, final int delay) {
		return nonUIAsyncExec(this.customExecutorService, clazz, purpose,
				callable, delay);
	}

	public Future<Void> customNonUIAsyncExec(Class<?> clazz, String purpose,
			final Runnable runnable, final int delay) {
		return nonUIAsyncExec(this.customExecutorService, clazz, purpose,
				runnable, delay);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection} and each in a new thread.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a list of {@link Future}s
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public <INPUT, OUTPUT> List<Future<OUTPUT>> customNonUIAsyncExec(
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(this.customExecutorService, input,
				parametrizedCallable);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection} and each in a new thread.
	 * 
	 * @param executorService
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a list of {@link Future}s
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public <INPUT, OUTPUT> List<Future<OUTPUT>> customNonUIAsyncExec(
			Class<?> clazz,
			String purpose,
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExec(EXECUTOR_SERVICE, clazz, purpose, input,
				parametrizedCallable);
	}

	/**
	 * Executes the given {@link ExecUtils.ParametrizedCallable} once per
	 * element in the given input {@link Collection}.
	 * <p>
	 * In contrast to
	 * {@link #nonUIAsyncExec(java.util.concurrent.ExecutorService, Collection, ParametrizedCallable)}
	 * this method returns a single {@link Future} containing all results.
	 * 
	 * @param input
	 *            whose elements are used as the parameter for the
	 *            {@link ExecUtils.ParametrizedCallable}
	 * @param parametrizedCallable
	 *            to be called n times
	 * @return a {@link Future} that contains the results
	 * 
	 * @UIThread
	 * @NonUIThread
	 */
	public <INPUT, OUTPUT> Iterable<OUTPUT> customNonUIAsyncExecMerged(
			Collection<INPUT> input,
			final ExecUtils.ParametrizedCallable<INPUT, OUTPUT> parametrizedCallable) {
		return nonUIAsyncExecMerged(this.customExecutorService, input,
				parametrizedCallable);
	}

}
