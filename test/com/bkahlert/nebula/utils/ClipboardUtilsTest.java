package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.bkahlert.nebula.utils.ClipboardListener.IClipboardContentChangeListener;

public class ClipboardUtilsTest {

	@Test
	public void testPerformance() throws Exception {
		IClipboardContentChangeListener listener = new IClipboardContentChangeListener() {
			@Override
			public void contentChanged() {
			}
		};

		long checkEvery = 200;
		long tolerance = (long) (2.5 * checkEvery);

		ClipboardListener clipboardListener = new ClipboardListener(checkEvery);
		clipboardListener.addClipboardContentChangeListener(listener);
		clipboardListener.start();

		ExecUtils.busyWait(1000);
		assertTrue(
				"At least one listener is registered, but the clipboard fast not checked in a timely manner",
				clipboardListener.lastCheck > System.currentTimeMillis()
						- tolerance);

		ExecUtils.busyWait(300);
		assertTrue(
				"At least one listener is registered, but the clipboard fast not checked in a timely manner",
				clipboardListener.lastCheck > System.currentTimeMillis()
						- tolerance);

		clipboardListener.removeClipboardContentChangeListener(listener);
		ExecUtils.busyWait(300);
		assertFalse("No listeners are registered, but the "
				+ ClipboardListener.class.getSimpleName()
				+ " is still checking",
				clipboardListener.lastCheck > System.currentTimeMillis()
						+ tolerance);

		ExecUtils.busyWait(2500);
		assertFalse("No listeners are registered, but the "
				+ ClipboardListener.class.getSimpleName()
				+ " is still checking",
				clipboardListener.lastCheck > System.currentTimeMillis()
						+ tolerance);

		clipboardListener.addClipboardContentChangeListener(listener);
		ExecUtils.busyWait(300);
		assertTrue(
				"At least one listener is registered, but the clipboard fast not checked in a timely manner",
				clipboardListener.lastCheck > System.currentTimeMillis()
						- tolerance);

		clipboardListener.requestStop();
		ExecUtils.busyWait(300);
		assertFalse(
				"Although stopped the "
						+ ClipboardListener.class.getSimpleName()
						+ " is still checking",
				clipboardListener.lastCheck > System.currentTimeMillis()
						+ tolerance);
	}
}
