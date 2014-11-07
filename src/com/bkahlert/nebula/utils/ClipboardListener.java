package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
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
 * <li>only polling if a {@link IClipboardContentChangeListener} is registered
 * <small>(Therefore it is recommended to unregister if possible.)</small></li>
 * </ul>
 * 
 * @author bkahlert
 * 
 */
public class ClipboardListener extends Thread {

	private static final Logger LOGGER = Logger
			.getLogger(ClipboardListener.class);

	public static interface IClipboardContentChangeListener {
		public void contentChanged();
	}

	public static interface IClipboardTransferChangeListener {
		public void transferChanged();
	}

	public static Map<String, Transfer> mimeTypeMapping;
	static {
		mimeTypeMapping = new HashMap<String, Transfer>();
		mimeTypeMapping.put("text/plain", TextTransfer.getInstance());
		mimeTypeMapping.put("text/url", URLTransfer.getInstance());
		mimeTypeMapping.put("image", ImageTransfer.getInstance());
		mimeTypeMapping.put("text/richtext", RTFTransfer.getInstance());
		mimeTypeMapping.put("text/rtf", RTFTransfer.getInstance());
		mimeTypeMapping.put("application/rtf", RTFTransfer.getInstance());
		mimeTypeMapping.put("text/html", HTMLTransfer.getInstance());
		mimeTypeMapping.put("application/file", FileTransfer.getInstance());
		mimeTypeMapping.put("application/x-java-file-list",
				FileTransfer.getInstance());
	}

	/**
	 * Used to allow testing.
	 */
	public Long lastCheck;

	final Clipboard clipboard;
	private final long checkEvery;
	private Object lastClipboardContent = null;
	private Object lastClipboardTransfer = null;
	private boolean stop = false;

	private final List<IClipboardContentChangeListener> clipboardContentChangeListeners = new ArrayList<IClipboardContentChangeListener>();
	private final List<IClipboardTransferChangeListener> clipboardTransferChangeListeners = new ArrayList<IClipboardTransferChangeListener>();

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

	public void addClipboardContentChangeListener(
			IClipboardContentChangeListener clipboardContentChangeListener) {
		if (this.clipboardContentChangeListeners
				.contains(clipboardContentChangeListener)) {
			return;
		}
		this.clipboardContentChangeListeners
				.add(clipboardContentChangeListener);
		this.stopHibernating();
	}

	public void removeClipboardContentChangeListener(
			IClipboardContentChangeListener clipboardContentChangeListener) {
		if (!this.clipboardContentChangeListeners
				.contains(clipboardContentChangeListener)) {
			return;
		}
		this.clipboardContentChangeListeners
				.remove(clipboardContentChangeListener);
	}

	public void addClipboardTransferChangeListener(
			IClipboardTransferChangeListener clipboardTransferChangeListener) {
		if (this.clipboardTransferChangeListeners
				.contains(clipboardTransferChangeListener)) {
			return;
		}
		this.clipboardTransferChangeListeners
				.add(clipboardTransferChangeListener);
		this.stopHibernating();
	}

	public void removeClipboardTransferChangeListener(
			IClipboardTransferChangeListener clipboardTransferChangeListener) {
		if (!this.clipboardTransferChangeListeners
				.contains(clipboardTransferChangeListener)) {
			return;
		}
		this.clipboardTransferChangeListeners
				.remove(clipboardTransferChangeListener);
	}

	@Override
	public void run() {
		ExecUtils.setThreadLabel(ClipboardListener.class, "Checking clipboard");
		while (true) {
			this.hibernateIfNecessary();

			TimePassed passed = new TimePassed(true, "Clipboard Check");
			if (this.clipboardContentChangeListeners.size() > 0) {
				Object newClipboardContent = this.getClipboardContent();
				if (!ObjectUtils.equals(newClipboardContent,
						this.lastClipboardContent)) {
					LOGGER.debug("Clipboard content changed from "
							+ this.lastClipboardContent + " to "
							+ newClipboardContent);
					ExecUtils.asyncExec(new Runnable() {
						@Override
						public void run() {
							for (IClipboardContentChangeListener clipboardContentChangeListener : ClipboardListener.this.clipboardContentChangeListeners) {
								try {
									clipboardContentChangeListener
											.contentChanged();
								} catch (Exception e) {
									LOGGER.error(e);
								}
							}
						}
					});
					this.lastClipboardContent = newClipboardContent;
				}
			}
			if (this.clipboardTransferChangeListeners.size() > 0) {
				Transfer newClipboardTransfer = this.getClipboardTransfer();
				if (!ObjectUtils.equals(newClipboardTransfer,
						this.lastClipboardTransfer)) {
					LOGGER.debug("Clipboard transfer changed from "
							+ this.lastClipboardTransfer + " to "
							+ newClipboardTransfer);
					ExecUtils.asyncExec(new Runnable() {
						@Override
						public void run() {
							for (IClipboardTransferChangeListener clipboardTransferChangeListener : ClipboardListener.this.clipboardTransferChangeListeners) {
								try {
									clipboardTransferChangeListener
											.transferChanged();
								} catch (Exception e) {
									LOGGER.error(e);
								}
							}
						}
					});
					this.lastClipboardTransfer = newClipboardTransfer;
				}
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
		if (this.clipboardContentChangeListeners.size() == 0
				&& this.clipboardTransferChangeListeners.size() == 0) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
	}

	private synchronized void stopHibernating() {
		if (this.clipboardContentChangeListeners.size() > 0
				|| this.clipboardTransferChangeListeners.size() > 0) {
			this.notifyAll();
		}
	}

	private final Transfer[] hashTransfers = new Transfer[] {
			LocalSelectionTransfer.getTransfer(), RTFTransfer.getInstance(),
			HTMLTransfer.getInstance(), FileTransfer.getInstance(),
			ImageTransfer.getInstance(), URLTransfer.getInstance(),
			TextTransfer.getInstance() };

	/**
	 * Returns the most valuable {@link Transfer}. This is the one that should
	 * most likely be used.
	 * 
	 * @return
	 */
	private Transfer getClipboardTransfer() {
		try {
			Transfer transfer = ExecUtils.syncExec(new Callable<Transfer>() {
				@Override
				public Transfer call() throws Exception {
					for (Transfer transfer : ClipboardListener.this.hashTransfers) {
						try {
							Object content = ClipboardListener.this.clipboard
									.getContents(transfer);
							if (content != null) {
								return transfer;
							}
						} catch (NullPointerException e) {
							// e.g. happens if a copied file was deleted in the
							// mean time
						}
					}
					return null;
				}
			});
			return transfer;
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return null;
	}

	/**
	 * Calculates the most valuable {@link Clipboard} content. This is the one
	 * that should most likely be used.
	 * 
	 * @return
	 */
	private Object getClipboardContent() {
		try {
			Object content = ExecUtils.syncExec(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Transfer transfer = ClipboardListener.this
							.getClipboardTransfer();
					if (transfer != null) {
						Object content = ClipboardListener.this.clipboard
								.getContents(transfer);
						if (content != null) {
							return content;
						}
					}
					return null;
				}
			});
			if (content != null) {
				return content;
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return new Object();
	}

	public void requestStop() {
		this.stop = true;
	}
}
