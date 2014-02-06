package com.bkahlert.devel.nebula.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class OffWorker {

	private final ArrayBlockingQueue<FutureTask<?>> queue;
	private final Thread runner;

	public OffWorker(int capacity) {
		this.queue = new ArrayBlockingQueue<FutureTask<?>>(capacity, true);
		this.runner = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						FutureTask<?> task = OffWorker.this.queue.take();
						task.run();
					} catch (InterruptedException e) {
						throw new RuntimeException(
								"Error getting next task to run", e);
					}
				}
			}
		}, OffWorker.class.getSimpleName());
	}

	public void start() {
		this.runner.start();
	}

	public <V> Future<V> submit(final Callable<V> callable) {
		FutureTask<V> task = new FutureTask<V>(callable);
		if (!this.queue.add(task)) {
			throw new RuntimeException("Capacity (" + this.queue.size()
					+ ") of " + this.getClass().getSimpleName() + " exceeded!");
		}
		return task;
	}

}
