package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.OffWorker;
import com.bkahlert.nebula.utils.OffWorker.StateException;

public class OffWorkerTest {

	public static final class Task implements Callable<Long> {
		private final int i;

		public Task(int i) {
			this.i = i;
		}

		@Override
		public Long call() throws Exception {
			System.out.println("Running " + Task.class.getSimpleName() + " #"
					+ this.i + " in " + Thread.currentThread());
			Thread.sleep(2);
			return System.currentTimeMillis();
		}
	}

	@Test
	public void testNonUIAsyncExecMerged() throws InterruptedException,
			ExecutionException, TimeoutException {
		int numTasks = 1500;
		// int delay = 2000;

		final OffWorker offWorker = new OffWorker(OffWorkerTest.class, "Test");
		List<Future<Long>> futures = new ArrayList<Future<Long>>();

		for (int i = 0; i < numTasks; i++) {
			Future<Long> future = offWorker.submit(new Task(i));
			futures.add(future);

			if (i == numTasks / 2) {
				ExecUtils.nonUIAsyncExec(new Runnable() {
					@Override
					public void run() {
						offWorker.start();
					}
				});
			}
		}

		// TimePassed passed = new TimePassed(true);

		assertEquals(numTasks, futures.size());

		long lastTimestamp = -1;
		for (Future<Long> future : futures) {
			long timestamp = future.get();
			assertTrue(timestamp > lastTimestamp);
			lastTimestamp = timestamp;
		}

		System.out.println("Running task after all have finished");
		assertTrue(offWorker.submit(new Task(numTasks)).get() > lastTimestamp);

		offWorker.shutdown();

		assertTrue(offWorker.isShutdown());

		try {
			offWorker.start();
			assertTrue(false);
		} catch (StateException e) {
		}

		try {
			offWorker.pause();
			assertTrue(false);
		} catch (StateException e) {
		}

		try {
			offWorker.unpause();
			assertTrue(false);
		} catch (StateException e) {
		}

		// assertTrue("Only " + passed.getTimePassed() + "ms instead of " +
		// delay
		// + "ms have passed.", passed.getTimePassed() > delay);
		assertEquals(true, true);
	}

}
