package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.widgets.timeline.impl.TimePassed;

/**
 * Instances of this class notify listeners about {@link Clipboard} content
 * changes.
 * <p>
 * Due to missing Java functionality / OS restrictions this class polls the
 * {@link Clipboard}. It limits performance penalties by
 * <ul>
 * <li>only checking the text part of the {@link Clipboard}, <small>(If no text
 * part is present the {@link Clipboard} is always considered to have
 * changed.)</small></li>
 * <li>using the SWT instead of the AWT API, <small>(Checking by means of AWT
 * about 100ms are used. SWT consumes about 1ms.</small>)</li>
 * <li>only polling if a {@link IClipboardChangeListener} is registered
 * <small>(Therefore it is recommended to unregister if possible.)</small></li>
 * </ul>
 * 
 * @author bkahlert
 * 
 */
public class ClipboardListener extends Thread {

	private static final Logger LOGGER = Logger
			.getLogger(ClipboardListener.class);

	public static interface IClipboardChangeListener {
		public void contentChanged();
	}

	/**
	 * Used to allow testing.
	 */
	public Long lastCheck;

	final Clipboard clipboard;
	private final long checkEvery;
	private int lastHash = new Object().hashCode();
	private boolean stop = false;

	private final List<IClipboardChangeListener> clipboardChangeListeners = new ArrayList<IClipboardChangeListener>();

	public ClipboardListener(long checkEvery) {
		this.checkEvery = checkEvery;
		try {
			this.clipboard = ExecUtils.syncExec(new Callable<Clipboard>() {
				@Override
				public Clipboard call() throws Exception {
					return new Clipboard(Display.getCurrent());
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addClipboardChangeListener(
			IClipboardChangeListener clipboardChangeListener) {
		if (this.clipboardChangeListeners.contains(clipboardChangeListener)) {
			return;
		}
		boolean stopHibernating = this.clipboardChangeListeners.size() == 0;
		this.clipboardChangeListeners.add(clipboardChangeListener);
		if (stopHibernating) {
			this.stopHibernating();
		}
	}

	public void removeClipboardChangeListener(
			IClipboardChangeListener clipboardChangeListener) {
		if (!this.clipboardChangeListeners.contains(clipboardChangeListener)) {
			return;
		}
		this.clipboardChangeListeners.remove(clipboardChangeListener);
	}

	@Override
	public void run() {
		ExecUtils.setThreadLabel(ClipboardListener.class, "Checking clipboard");
		while (true) {
			this.hibernateIfNecessary();

			TimePassed passed = new TimePassed(true, "Clipboard Check");
			int newHash = this.hashClipboard();

			if (newHash != this.lastHash) {
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						for (IClipboardChangeListener clipboardChangeListener : ClipboardListener.this.clipboardChangeListeners) {
							try {
								clipboardChangeListener.contentChanged();
							} catch (Exception e) {
								LOGGER.error(e);
							}
						}
					}
				});
				this.lastHash = newHash;
			}
			passed.finished();
			this.lastCheck = System.currentTimeMillis();

			if (this.stop) {
				break;
			}

			this.hibernateIfNecessary();
			try {
				Thread.sleep(this.checkEvery);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
	}

	private synchronized void hibernateIfNecessary() {
		if (this.clipboardChangeListeners.size() == 0) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
	}

	private synchronized void stopHibernating() {
		this.notifyAll();
	}

	/**
	 * Calculates hash on the system clipboard.
	 * <p>
	 * TODO: Currently only uses the string representation if present.
	 * 
	 * @return
	 */
	private int hashClipboard() {
		try {
			String clipboardText = (String) ExecUtils
					.syncExec(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							return ClipboardListener.this.clipboard
									.getContents(TextTransfer.getInstance());
						}
					});
			if (clipboardText != null) {
				return clipboardText.hashCode();
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return new Object().hashCode();
	}

	public void requestStop() {
		this.stop = true;
	}
}
