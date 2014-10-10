package com.bkahlert.nebula.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.Assert;

/**
 * This {@link Future} wraps a given {@link Future} and applies a
 * {@link IConverter} to its return value.
 * 
 * @author bkahlert
 * 
 * @param <V>
 * @param <W>
 */
public class ConvertingFuture<V, W> implements Future<W> {

	private final Future<V> future;
	private final IConverter<V, W> converter;

	public ConvertingFuture(Future<V> future, IConverter<V, W> converter) {
		Assert.isNotNull(future);
		Assert.isNotNull(converter);
		this.future = future;
		this.converter = converter;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future.cancel(mayInterruptIfRunning);
	}

	@Override
	public W get() throws InterruptedException, ExecutionException {
		V rs = this.future.get();
		return this.converter.convert(rs);
	}

	@Override
	public W get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		V rs = this.future.get(timeout, unit);
		return this.converter.convert(rs);
	}

	@Override
	public boolean isCancelled() {
		return this.future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.future.isDone();
	}

}
