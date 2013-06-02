package com.bkahlert.devel.nebula.utils;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.bkahlert.devel.nebula.utils.ExecutorService.ParametrizedCallable;

public class ExecutorUtil {

	private static final com.bkahlert.devel.nebula.utils.ExecutorService EXECUTOR_SERVICE = new com.bkahlert.devel.nebula.utils.ExecutorService();

	/**
	 * @see com.bkahlert.devel.nebula.utils.ExecutorService#syncExec(Callable)
	 */
	public static <V> V syncExec(final Callable<V> callable) throws Exception {
		return EXECUTOR_SERVICE.syncExec(callable);
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
		EXECUTOR_SERVICE.syncExec(runnable);
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
		return EXECUTOR_SERVICE.asyncExec(callable);
	}

	/**
	 * Executes the given {@link Runnable} asynchronously in the UI thread.
	 * 
	 * @param runnable
	 */
	public static void asyncExec(Runnable runnable) {
		EXECUTOR_SERVICE.asyncExec(runnable);
	}

	/**
	 * Executes the given {@link Runnable} delayedly and asynchronously in the
	 * UI thread.
	 * 
	 * @param runnable
	 * @param delay
	 */
	public static void asyncExec(final Runnable runnable, final long delay) {
		EXECUTOR_SERVICE.asyncExec(runnable, delay);
	}

	/**
	 * Runs the given {@link Runnable} in a non-UI thread. If the caller already
	 * runs in such one the {@link Runnable} is simply executed. Otherwise a new
	 * thread is started.
	 * 
	 * @param runnable
	 */
	public static void nonUIExec(Runnable runnable) {
		EXECUTOR_SERVICE.nonUIExec(runnable);
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
		return EXECUTOR_SERVICE.nonUIAsyncExec(callable);
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
		EXECUTOR_SERVICE.nonUIAsyncExec(runnable);
	}

	public static <T, V> List<Future<V>> nonUIAsyncExec(
			Collection<T> collection,
			final ParametrizedCallable<T, V> parametrizedCallable) {
		return EXECUTOR_SERVICE
				.nonUIAsyncExec(collection, parametrizedCallable);
	}

	/**
	 * Runs the runnable in a separate {@link Thread} and returns it.
	 * 
	 * @param
	 * @return
	 */
	public static Thread asyncRun(Runnable runnable) {
		return EXECUTOR_SERVICE.asyncRun(runnable);
	}

	/**
	 * Runs the runnable delayedly in the UI {@link Thread} and returns it.
	 * 
	 * @param
	 * @return the thread that is delayedly executed
	 */
	public static void asyncRun(final Runnable runnable, final long delay) {
		EXECUTOR_SERVICE.asyncExec(runnable, delay);
	}

	public static Future<?> nonUIAsyncRun(final Runnable runnable,
			final long delay) {
		return EXECUTOR_SERVICE.nonUIAsyncRun(runnable, delay);
	}

	private ExecutorUtil() {
	}
}
