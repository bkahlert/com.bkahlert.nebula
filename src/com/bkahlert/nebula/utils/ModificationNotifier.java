package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * This class handles the notification of {@link ModifyEvent}s and is meant to
 * be used by {@link Control} that want to offer {@link ModifyEvent}s.
 * <p>
 * You are expected to call {@link #modified(Object)} every time the
 * {@link Control}'s value changed. This will result in a delayed
 * {@link ModifyEvent}. To accelerate things you can use {@link #notifyNow()} to
 * trigger the delayed event immediately. This is useful if the contents should
 * be replaced.
 * <p>
 * <b>Please not that {@link #notifyNow()} is automatically called if the
 * {@link Control} is being disposed.</b> Simply make sure to have
 * {@link #modifyied(Object)} called before.
 * 
 * @author bkahlert
 * 
 */
public class ModificationNotifier<T> implements IModifiable {

	private final Timer timer = new Timer(this.getClass().getSimpleName()
			+ " :: Delay Modification Timer", false);
	private TimerTask task = null;

	private final List<ModifyListener> modifyListeners = new ArrayList<ModifyListener>();

	private final Control control;
	private T oldValue = null;
	private final long delayChangeEventTo;

	public ModificationNotifier(final Control control, long delayChangeEventTo) {
		this.control = control;
		this.delayChangeEventTo = delayChangeEventTo;

		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				// there might be other DisposeListeners
				// with asyncExec we make sure we don't stop the timer to
				// early
				ExecUtils.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (control) {
							if (ModificationNotifier.this.timer != null) {
								ModificationNotifier.this.timer.cancel();
							}
							ModificationNotifier.this.notifyNow();
						}
					}
				});

			}
		});
	}

	/**
	 * Informs the {@link ModificationNotifier} that the {@link Control}'s
	 * content changed. This will trigger a delayed {@link ModifyEvent}.
	 * 
	 * @param newValue
	 */
	public void modified(final T newValue) {
		if (ObjectUtils.equals(this.oldValue, newValue)) {
			return;
		}

		this.oldValue = newValue;

		final Runnable fireRunnable = new Runnable() {
			@Override
			public void run() {
				Event event = new Event();
				event.display = Display.getCurrent();
				event.widget = ModificationNotifier.this.control;
				event.text = newValue instanceof String ? (String) newValue
						: null;
				event.data = newValue;
				ModifyEvent modifyEvent = new ModifyEvent(event);
				for (ModifyListener modifyListener : ModificationNotifier.this.modifyListeners) {
					modifyListener.modifyText(modifyEvent);
				}
			}
		};

		synchronized (this.control) {
			if (ModificationNotifier.this.task != null) {
				ModificationNotifier.this.task.cancel();
			}
			if (ModificationNotifier.this.delayChangeEventTo > 0) {
				ModificationNotifier.this.task = new TimerTask() {
					@Override
					public void run() {
						fireRunnable.run();
					}
				};
				ModificationNotifier.this.timer.schedule(
						ModificationNotifier.this.task,
						ModificationNotifier.this.delayChangeEventTo);
			} else {
				ModificationNotifier.this.task = null;
				fireRunnable.run();
			}
		}
	}

	public void notifyNow() {
		/*
		 * do not wait for the delay to pass but invoke the task immediately
		 */
		synchronized (this.control) {
			if (this.task != null) {
				this.task.cancel();
				this.task.run();
				this.task = null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.nebula.widgets.composer.IModificationNotifier#
	 * addModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
	@Override
	public void addModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.add(modifyListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.nebula.widgets.composer.IModificationNotifier#
	 * removeModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
	@Override
	public void removeModifyListener(ModifyListener modifyListener) {
		this.modifyListeners.remove(modifyListener);
	}

}