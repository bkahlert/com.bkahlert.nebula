package com.bkahlert.nebula.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletedFuture<V> implements Future<V> {

	private V value;
	private Exception exception;

	public CompletedFuture(V value, Exception exception) {
		this.value = value;
		this.exception = exception;
	}

	public CompletedFuture(Callable<V> callable) {
		this.value = null;
		this.exception = null;
		try {
			this.value = callable.call();
		} catch (Exception e) {
			this.exception = e;
		}
	}

	public CompletedFuture(Runnable runnable) {
		this.value = null;
		this.exception = null;
		try {
			runnable.run();
		} catch (Exception e) {
			this.exception = e;
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		if (this.exception != null) {
			throw new ExecutionException(this.exception);
		}
		return this.value;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return this.get();
	}

}
