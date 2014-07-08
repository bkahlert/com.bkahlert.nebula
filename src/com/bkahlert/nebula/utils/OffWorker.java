package com.bkahlert.nebula.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

public class OffWorker {

	public static class StateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public StateException(State oldState, State newState) {
			super("Cannot change from state " + oldState + " to " + newState);
		}
	}

	private static enum State {
		INIT, RUNNING, PAUSED, SHUTDOWN
	}

	private final LinkedBlockingQueue<FutureTask<?>> queue;
	private final Thread runner;
	private State state = State.INIT;

	public OffWorker(Class<?> owner, String purpose) {
		this.queue = new LinkedBlockingQueue<FutureTask<?>>();
		this.runner = new Thread(new Runnable() {
			@Override
			public void run() {
				loop: while (true) {
					boolean isInterrupted = Thread.interrupted();
					if (!isInterrupted) {
						try {
							if (OffWorker.this.state == State.PAUSED) {
								this.wait();
							}
							FutureTask<?> task = OffWorker.this.queue.take();
							task.run();
						} catch (InterruptedException e) {
							isInterrupted = true;
						}
					}
					if (isInterrupted) {
						switch (OffWorker.this.state) {
						case INIT:
							throw new RuntimeException("Implementation Error");
						case RUNNING:
							break;
						case PAUSED:
							break;
						case SHUTDOWN:
							break loop;
						}
					}
				}
			}
		}, owner.getSimpleName() + " :: " + purpose + " :: "
				+ OffWorker.class.getSimpleName());
	}

	public void start() {
		switch (this.state) {
		case INIT:
			this.state = State.RUNNING;
			this.runner.start();
			break;
		case RUNNING:
			throw new StateException(State.RUNNING, State.RUNNING);
		case PAUSED:
			this.unpause();
			break;
		case SHUTDOWN:
			throw new StateException(State.SHUTDOWN, State.RUNNING);
		}
	}

	public void pause() {
		switch (this.state) {
		case INIT:
			throw new StateException(State.INIT, State.PAUSED);
		case RUNNING:
			this.state = State.PAUSED;
			this.runner.interrupt();
		case PAUSED:
			break;
		case SHUTDOWN:
			throw new StateException(State.SHUTDOWN, State.PAUSED);
		}
	}

	public void unpause() {
		switch (this.state) {
		case INIT:
			this.start();
			break;
		case RUNNING:
			break;
		case PAUSED:
			this.state = State.RUNNING;
			this.runner.interrupt();
			break;
		case SHUTDOWN:
			throw new StateException(State.SHUTDOWN, State.RUNNING);
		}
	}

	public void shutdown() {
		switch (this.state) {
		case INIT:
			this.state = State.SHUTDOWN;
			break;
		case RUNNING:
			this.state = State.SHUTDOWN;
			this.runner.interrupt();
		case PAUSED:
			this.state = State.SHUTDOWN;
			this.runner.interrupt();
			break;
		case SHUTDOWN:
			this.state = State.SHUTDOWN;
			this.runner.interrupt();
		}
	}

	public boolean isShutdown() {
		return this.state == State.SHUTDOWN;
	}

	public synchronized <V> Future<V> submit(final Callable<V> callable) {
		return this.submit(callable, null);
	}

	public synchronized <V> Future<V> submit(final Callable<V> callable,
			final String name) {
		FutureTask<V> task = new FutureTask<V>(
				name != null ? new Callable<V>() {
					@Override
					public V call() throws Exception {
						String label = ExecUtils.backupThreadLabel();
						Thread.currentThread().setName(
								label + " :: Running " + name);
						try {
							return callable.call();
						} finally {
							ExecUtils.restoreThreadLabel();
						}
					}
				} : callable);
		if (!this.queue.add(task)) {
			throw new RuntimeException("Capacity (" + this.queue.size()
					+ ") of " + this.getClass().getSimpleName() + " exceeded!");
		}
		return task;
	}

	public void finish() {
		this.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				OffWorker.this.shutdown();
				return null;
			}
		});
	}

}
