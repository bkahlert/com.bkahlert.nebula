package com.bkahlert.devel.nebula.widgets.timelineGroup.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

/**
 * This widget display one or more timelines.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINE>
 *            that should be used internally
 */
public class TimelineGroup<TIMELINE extends IBaseTimeline> extends Composite
		implements ITimelineGroup<TIMELINE> {

	public static interface ITimelineFactory<T extends IBaseTimeline> {
		public T createTimeline(Composite parent, int style);
	}

	private static final Logger LOGGER = Logger.getLogger(TimelineGroup.class);

	private ListenerList timelinesListeners = new ListenerList();
	private ITimelineFactory<TIMELINE> timelineFactory;

	public TimelineGroup(Composite parent, int style,
			ITimelineFactory<TIMELINE> timelineFactory) {
		super(parent, style);
		super.setLayout(new FillLayout(SWT.VERTICAL));
		this.timelineFactory = timelineFactory;
	}

	@Override
	public <T> Future<T> show(Set<ITimelineInput> inputs,
			IProgressMonitor monitor, final Callable<T> success) {
		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				2 + inputs.size() + 1);

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		ITimelineInput[] unpreparedInputs = prepareTimelines(inputs);
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		monitor.worked(2);

		for (final ITimelineInput unpreparedInput : unpreparedInputs) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final AtomicReference<TIMELINE> timeline = new AtomicReference<TIMELINE>();
			try {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						timeline.set(getTimeline(unpreparedInput));
					}
				};
				if (Display.getCurrent() == Display.getDefault())
					runnable.run();
				else
					Display.getDefault().syncExec(runnable);
			} catch (Exception e) {
				LOGGER.error("Error retrieving "
						+ Timeline.class.getSimpleName() + " for "
						+ unpreparedInput);
				continue;
			}

			if (timeline.get() == null) {
				// timeline was not prepared -> create a new one
				try {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							final TIMELINE createdTimeline = timelineFactory
									.createTimeline(TimelineGroup.this,
											SWT.NONE);
							timeline.set(createdTimeline);
							if (createdTimeline instanceof ITimeline) {
								((ITimeline) createdTimeline)
										.addTimelineListener(new ITimelineListener() {
											@Override
											public void clicked(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.clicked(event);
											}

											@Override
											public void middleClicked(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.middleClicked(event);
											}

											@Override
											public void rightClicked(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.rightClicked(event);
											}

											@Override
											public void doubleClicked(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.doubleClicked(event);
											}

											@Override
											public void hoveredIn(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.hoveredIn(event);
											}

											@Override
											public void hoveredOut(
													TimelineEvent event) {
												Object[] listeners = timelinesListeners
														.getListeners();
												for (int i = 0, m = listeners.length; i < m; ++i)
													((ITimelineListener) listeners[i])
															.hoveredOut(event);
											}
										});
							}
							((Control) createdTimeline)
									.setData(unpreparedInput);
						}
					};
					if (Display.getCurrent() == Display.getDefault())
						runnable.run();
					else
						Display.getDefault().syncExec(runnable);
				} catch (Exception e) {
					LOGGER.error("Error creating " + Timeline.class + " for "
							+ unpreparedInput);
					continue;
				}
			} else {
				// a no more needed timeline was associated with an unprepared
				// key
			}

			// init timeline viewer
			timeline.get().show(unpreparedInput, subMonitor.newChild(1));

			if (monitor.isCanceled()) {
				disposeTimelines(unpreparedInput);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = Executors.newSingleThreadExecutor().submit(
				new Callable<T>() {
					@Override
					public T call() throws Exception {
						final AtomicReference<T> r = new AtomicReference<T>();
						final AtomicReference<Exception> exception = new AtomicReference<Exception>();
						final Semaphore mutex = new Semaphore(0);
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									TimelineGroup.this.layout();
									if (success != null)
										r.set(success.call());
									else
										r.set(null);
								} catch (Exception e) {
									exception.set(e);
								}
								mutex.release();
							}
						});
						mutex.acquire();
						if (exception.get() != null)
							throw exception.get();
						return r.get();
					}
				});

		monitor.worked(1);
		monitor.done();

		return rs;
	}

	/**
	 * Returns all {@link ITimelineInput}s the created timelines are associated
	 * with.
	 * 
	 * @return
	 */
	public Set<ITimelineInput> getTimelineKeys() {
		final Set<ITimelineInput> keys = new HashSet<ITimelineInput>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Control control : TimelineGroup.this.getChildren()) {
					if (!control.isDisposed() && control instanceof Timeline) {
						keys.add((ITimelineInput) control.getData());
					}
				}
			}
		});
		return keys;
	}

	/**
	 * Returns the timeline that is associated with the given input.
	 * 
	 * @UI must be called from the UI thread
	 * @param input
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TIMELINE getTimeline(final ITimelineInput input) {
		Assert.isNotNull(input);
		for (Control control : this.getChildren()) {
			if (!control.isDisposed() && control instanceof Timeline) {
				if (input.equals(control.getData())) {
					return (TIMELINE) control;
				}
			}
		}
		return null;
	}

	/**
	 * Prepares already instantiated timelines in the following way:
	 * <ol>
	 * <li>timelines already identified with a given key stay untouched since
	 * they are already working
	 * <li>the other timelines are associated with a new key since the key they
	 * are still identified with if no longer requested
	 * <li>timelines that are not needed anymore become disposed (since all
	 * requested keys are treated already)
	 * <li>all newly assigned keys and unassigned keys are returned (so the
	 * caller can load the actual contents)
	 * </ol>
	 * 
	 * @param inputs
	 * @return keys that were not associated to an existing timeline or were
	 *         associated with a timeline that before was responsible for
	 *         another key
	 */
	private ITimelineInput[] prepareTimelines(Set<ITimelineInput> inputs) {
		List<ITimelineInput> neededTimelines = new LinkedList<ITimelineInput>(
				inputs);
		List<ITimelineInput> existingTimelines = new LinkedList<ITimelineInput>(
				getTimelineKeys());
		@SuppressWarnings("unchecked")
		List<ITimelineInput> preparedTimelines = ListUtils.intersection(
				existingTimelines, neededTimelines);
		@SuppressWarnings("unchecked")
		final List<ITimelineInput> unpreparedTimelines = ListUtils.subtract(
				neededTimelines, preparedTimelines);
		@SuppressWarnings("unchecked")
		final List<ITimelineInput> freeTimelines = ListUtils.subtract(
				existingTimelines, preparedTimelines);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (freeTimelines.size() > 0
						&& unpreparedTimelines.size() > i) {
					try {
						TIMELINE timeline = getTimeline(freeTimelines.remove(0));
						((Control) timeline).setData(unpreparedTimelines.get(i));
					} catch (Exception e) {
						LOGGER.error("Error assigning new key "
								+ unpreparedTimelines.get(i) + " to "
								+ Timeline.class);
					}
				}
			}
		});
		disposeTimelines(freeTimelines.toArray(new ITimelineInput[0]));
		return unpreparedTimelines.toArray(new ITimelineInput[0]);
	}

	/**
	 * Disposes all timelines that are identified by at least one of the
	 * provided keys.
	 * 
	 * @param inputs
	 */
	private void disposeTimelines(final ITimelineInput... inputs) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (ITimelineInput input : inputs) {
					try {
						TIMELINE selectionTimeline = getTimeline(input);
						if (selectionTimeline != null
								&& !((Control) selectionTimeline).isDisposed())
							((Control) selectionTimeline).dispose();
					} catch (Exception e) {
						LOGGER.error("Error disposing " + Timeline.class);
					}
				}
				TimelineGroup.this.layout();
			}
		};
		if (Display.getDefault() == Display.getCurrent())
			runnable.run();
		else
			Display.getDefault().asyncExec(runnable);
	}

	@Override
	public void setLayout(Layout layout) {
		return;
	}

	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelinesListeners.add(timelineListener);
	}

	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelinesListeners.remove(timelineListener);
	}
}
