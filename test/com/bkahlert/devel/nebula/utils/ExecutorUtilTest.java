package com.bkahlert.devel.nebula.utils;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.bkahlert.devel.nebula.utils.ExecutorService.DelayableThread;

public class ExecutorUtilTest {

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

}
