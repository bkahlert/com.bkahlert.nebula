package com.bkahlert.devel.nebula.widgets.timelineGroup.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;
import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;

/**
 * This widget display one or more {@code TIMELINE}s.
 * 
 * @author bkahlert
 */
public class TimelineGroup<TIMELINE extends ITimeline> extends Composite
		implements ITimelineGroup<TIMELINE> {

	private static final Logger LOGGER = Logger.getLogger(TimelineGroup.class);

	private ITimelineFactory<TIMELINE> timelineFactory;

	private ListenerList timelineListeners = new ListenerList();

	private ITimelineListener timelineListenerDelegate = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).clicked(event);
			}
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).middleClicked(event);
			}
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).rightClicked(event);
			}
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).doubleClicked(event);
			}
		}

		@Override
		public void resizeStarted(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resizeStarted(event);
			}
		}

		@Override
		public void resizing(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resizing(event);
			}
		}

		@Override
		public void resized(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resized(event);
			}
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).hoveredIn(event);
			}
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
			Object[] listeners = timelineListeners.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).hoveredOut(event);
			}
		}
	};

	public TimelineGroup(Composite parent, int style,
			ITimelineFactory<TIMELINE> timelineFactory) {
		super(parent, style);
		super.setLayout(new FillLayout(SWT.VERTICAL));

		Assert.isNotNull(timelineFactory);
		this.timelineFactory = timelineFactory;
	}

	@Override
	public <T> Future<T> show(Set<ITimelineInput> inputs,
			IProgressMonitor monitor, final Callable<T> success) {

		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				2 + (10 * inputs.size()) + 1);

		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		ITimelineInput[] unpreparedInputs = prepareTimelines(inputs);

		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		subMonitor.worked(2);

		for (final ITimelineInput unpreparedInput : unpreparedInputs) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			TIMELINE timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return getTimeline(unpreparedInput);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving "
						+ Timeline.class.getSimpleName() + " for "
						+ unpreparedInput);
				continue;
			}

			if (timeline == null) {
				// timeline was not prepared -> create a new one
				try {
					timeline = ExecutorUtil.syncExec(new Callable<TIMELINE>() {
						@Override
						public TIMELINE call() throws Exception {
							TIMELINE timeline = createTimeline();
							timeline.setData(unpreparedInput);
							return timeline;
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error creating " + Timeline.class + " for "
							+ unpreparedInput);
					continue;
				}
			} else {
				// a no more needed timeline was associated with an unprepared
				// key
			}

			subMonitor.worked(2);

			timeline.show(unpreparedInput, 300, 300, subMonitor.newChild(8));

			if (subMonitor.isCanceled()) {
				disposeTimelines(unpreparedInput);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = ExecutorUtil.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				TimelineGroup.this.layout();
				if (success != null)
					return success.call();
				return null;
			}
		});

		subMonitor.worked(1);
		subMonitor.done();

		return rs;
	}

	@Override
	public TIMELINE createTimeline() {
		final TIMELINE timeline = TimelineGroup.this.timelineFactory
				.createTimeline(TimelineGroup.this, SWT.NONE);
		timeline.addTimelineListener(timelineListenerDelegate);
		timeline.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				timeline.addTimelineListener(timelineListenerDelegate);
			}
		});
		return timeline;
	}

	public Set<ITimelineInput> getTimelineKeys() {
		final Set<ITimelineInput> inputs = new HashSet<ITimelineInput>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				for (Control control : TimelineGroup.this.getChildren()) {
					if (!control.isDisposed() && control instanceof Timeline) {
						inputs.add((ITimelineInput) control.getData());
					}
				}
			}
		});
		return inputs;
	}

	/**
	 * Returns the {@code TIMELINE} that is associated with the given key.
	 * 
	 * @UI must be called from the UI thread
	 * @param input
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TIMELINE getTimeline(final ITimelineInput input) {
		Assert.isNotNull(input);
		for (Control control : this.getChildren()) {
			if (!control.isDisposed() && control instanceof IBaseTimeline) {
				IBaseTimeline timeline = (IBaseTimeline) control;
				if (input.equals(timeline.getData())) {
					try {
						return (TIMELINE) timeline;
					} catch (Exception e) {
						LOGGER.fatal("Could not cast to generic type. "
								+ "It should never have happened that an "
								+ "incompatible timeline type was used.", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Prepares already instantiated {@code TIMELINE}s in the following way:
	 * <ol>
	 * <li>{@link SelectionTimeline}s already identified with a given key stay
	 * untouched since they are already working
	 * <li>the other {@link SelectionTimeline}s are associated with a new key
	 * since the key they are still identified with if no longer requested
	 * <li>{@link SelectionTimeline}s that are not needed anymore become
	 * disposed (since all requested keys are treated already)
	 * <li>all newly assigned keys and unassigned keys are returned (so the
	 * caller can load the actual contents)
	 * </ol>
	 * 
	 * @param inputs
	 * @return keys that were not associated to an existing
	 *         {@link SelectionTimeline} or were associated with a
	 *         {@link SelectionTimeline} that before was responsible for another
	 *         key
	 */
	private ITimelineInput[] prepareTimelines(Set<ITimelineInput> inputs) {
		List<Object> neededTimelines = new LinkedList<Object>(inputs);
		List<Object> existingTimelines = new LinkedList<Object>(
				getTimelineKeys());
		List<?> preparedTimelines = ListUtils.intersection(existingTimelines,
				neededTimelines);
		final List<?> unpreparedTimelines = ListUtils.subtract(neededTimelines,
				preparedTimelines);
		final List<ITimelineInput> freeTimelines = ArrayUtils.getInstances(
				ListUtils.subtract(existingTimelines, preparedTimelines)
						.toArray(), ITimelineInput.class);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (freeTimelines.size() > 0
						&& unpreparedTimelines.size() > i) {
					try {
						TIMELINE timeline = getTimeline(freeTimelines.remove(0));
						timeline.setData(unpreparedTimelines.get(i));
					} catch (Exception e) {
						LOGGER.error("Error assigning new key "
								+ unpreparedTimelines.get(i) + " to "
								+ Timeline.class);
					}
				}
			}
		});
		disposeTimelines(freeTimelines.toArray(new ITimelineInput[0]));
		return new HashSet<ITimelineInput>(ArrayUtils.getInstances(
				unpreparedTimelines.toArray(), ITimelineInput.class))
				.toArray(new ITimelineInput[0]);
	}

	/**
	 * Disposes all {@link SelectionTimeline}s that are identified by at least
	 * one of the provided keys.
	 * 
	 * @param timelineInputs
	 */
	private void disposeTimelines(final ITimelineInput... timelineInputs) {
		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				for (ITimelineInput timelineInput : timelineInputs) {
					try {
						TIMELINE timeline = getTimeline(timelineInput);
						if (timeline != null && !timeline.isDisposed())
							timeline.dispose();
					} catch (Exception e) {
						LOGGER.error("Error disposing " + Timeline.class);
					}
				}
			}
		});
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				TimelineGroup.this.layout();
			}
		});
	}

	@Override
	public void setLayout(Layout layout) {
		return;
	}

	@Override
	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	@Override
	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

}
