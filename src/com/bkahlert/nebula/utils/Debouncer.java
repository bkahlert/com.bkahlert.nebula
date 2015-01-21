package com.bkahlert.nebula.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * (C) http://stackoverflow.com/questions/4742210/implementing-debounce-in-java
 *
 * @author bkahlert
 *
 * @param <T>
 */
public class Debouncer<T> {

	private final ScheduledExecutorService sched = Executors
			.newScheduledThreadPool(1);
	private final ConcurrentHashMap<T, TimerTask> delayedMap = new ConcurrentHashMap<T, TimerTask>();
	private final Callback<T> callback;
	private long interval;

	public static interface Callback<T> {
		public void call(T arg);
	}

	public Debouncer(Callback<T> c) {
		this.callback = c;
	}

	public void call(T key, long interval) {
		this.interval = interval;
		TimerTask task = new TimerTask(key);

		TimerTask prev;
		do {
			prev = this.delayedMap.putIfAbsent(key, task);
			if (prev == null) {
				this.sched.schedule(task, this.interval, TimeUnit.MILLISECONDS);
			}
		} while (prev != null && !prev.extend()); // Exit only if new task was
													// added to map, or existing
													// task was extended
													// successfully
	}

	public void terminate() {
		this.sched.shutdownNow();
	}

	// The task that wakes up when the wait time elapses
	private class TimerTask implements Runnable {
		private final T key;
		private long dueTime;
		private final Object lock = new Object();

		public TimerTask(T key) {
			this.key = key;
			this.extend();
		}

		public boolean extend() {
			synchronized (this.lock) {
				if (this.dueTime < 0) {
					return false;
				}
				this.dueTime = System.currentTimeMillis()
						+ Debouncer.this.interval;
				return true;
			}
		}

		@Override
		public void run() {
			synchronized (this.lock) {
				long remaining = this.dueTime - System.currentTimeMillis();
				if (remaining > 0) { // Re-schedule task
					Debouncer.this.sched.schedule(this, remaining,
							TimeUnit.MILLISECONDS);
				} else { // Mark as terminated and invoke callback
					this.dueTime = -1;
					try {
						Debouncer.this.callback.call(this.key);
					} finally {
						Debouncer.this.delayedMap.remove(this.key);
					}
				}
			}
		}
	}
}