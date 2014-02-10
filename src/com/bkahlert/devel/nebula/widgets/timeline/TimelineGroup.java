package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

/**
 * This widget display one or more {@code TIMELINE}s.
 * 
 * @author bkahlert
 */
public class TimelineGroup<TIMELINE extends ITimeline, INPUT> extends Composite {

	private static final Logger LOGGER = Logger.getLogger(TimelineGroup.class);

	private final ITimelineFactory<TIMELINE> timelineFactory;

	private final ListenerList timelineListeners = new ListenerList();

	private final ITimelineListener timelineListenerDelegate = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).clicked(event);
			}
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).middleClicked(event);
			}
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).rightClicked(event);
			}
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).doubleClicked(event);
			}
		}

		@Override
		public void resizeStarted(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resizeStarted(event);
			}
		}

		@Override
		public void resizing(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resizing(event);
			}
		}

		@Override
		public void resized(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).resized(event);
			}
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).hoveredIn(event);
			}
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
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

	public <T> Future<T> show(Map<INPUT, ITimelineInput> inputs,
			IProgressMonitor monitor, final Callable<T> success) {

		final SubMonitor subMonitor = SubMonitor.convert(monitor,
				2 + (10 * inputs.size()) + 1);

		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		List<INPUT> unpreparedKeys = this.prepareTimelines(inputs.keySet());

		if (subMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}

		subMonitor.worked(2);

		for (final INPUT unpreparedKey : unpreparedKeys) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			TIMELINE timeline;
			try {
				timeline = ExecUtils.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return TimelineGroup.this.getTimeline(unpreparedKey);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving "
						+ ITimeline.class.getSimpleName() + " for "
						+ unpreparedKey);
				continue;
			}

			if (timeline == null) {
				// timeline was not prepared -> create a new one
				try {
					timeline = ExecUtils.syncExec(new Callable<TIMELINE>() {
						@Override
						public TIMELINE call() throws Exception {
							TIMELINE timeline = TimelineGroup.this
									.createTimeline();
							timeline.setData(unpreparedKey);
							return timeline;
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error creating " + ITimeline.class + " for "
							+ unpreparedKey);
					continue;
				}
			} else {
				// a no more needed timeline was associated with an unprepared
				// key
			}

			subMonitor.worked(2);

			timeline.show(inputs.get(unpreparedKey), 300, 300,
					subMonitor.newChild(8));

			if (subMonitor.isCanceled()) {
				List<INPUT> unpreparedInput_ = new ArrayList<INPUT>();
				unpreparedInput_.add(unpreparedKey);
				this.disposeTimelines(unpreparedInput_);
				throw new OperationCanceledException();
			}
		}

		Future<T> rs = ExecUtils.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				TimelineGroup.this.layout();
				if (success != null) {
					return success.call();
				}
				return null;
			}
		});

		subMonitor.worked(1);
		subMonitor.done();

		return rs;
	}

	public TIMELINE createTimeline() {
		final TIMELINE timeline = TimelineGroup.this.timelineFactory
				.createTimeline(TimelineGroup.this, SWT.NONE);
		if (true) {
			return timeline;
		}
		timeline.addTimelineListener(this.timelineListenerDelegate);
		timeline.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				timeline.addTimelineListener(TimelineGroup.this.timelineListenerDelegate);
			}
		});
		return timeline;
	}

	public Set<INPUT> getTimelineKeys() {
		final Set<INPUT> inputs = new HashSet<INPUT>();
		Display.getDefault().syncExec(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				for (Control control : TimelineGroup.this.getChildren()) {
					if (!control.isDisposed() && control instanceof ITimeline) {
						inputs.add((INPUT) control.getData());
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
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TIMELINE getTimeline(final INPUT key) {
		Assert.isNotNull(key);
		for (Control control : this.getChildren()) {
			if (!control.isDisposed() && control instanceof ITimeline) {
				ITimeline timeline = (ITimeline) control;
				if (key.equals(timeline.getData())) {
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
	 * @param set
	 * @return keys that were not associated to an existing
	 *         {@link SelectionTimeline} or were associated with a
	 *         {@link SelectionTimeline} that before was responsible for another
	 *         key
	 */
	@SuppressWarnings("unchecked")
	private List<INPUT> prepareTimelines(Set<INPUT> set) {
		List<INPUT> neededTimelinesKeys = new LinkedList<INPUT>(set);
		List<INPUT> existingTimelinesKeys = new LinkedList<INPUT>(
				this.getTimelineKeys());
		List<?> preparedTimelinesKeys = ListUtils.intersection(
				existingTimelinesKeys, neededTimelinesKeys);
		final List<?> unpreparedTimelinesKeys = ListUtils.subtract(
				neededTimelinesKeys, preparedTimelinesKeys);
		final List<INPUT> freeTimelinesKeys = new ArrayList<INPUT>();
		for (Object freeTimelineKey : ListUtils.subtract(existingTimelinesKeys,
				preparedTimelinesKeys)) {
			freeTimelinesKeys.add((INPUT) freeTimelineKey);
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				while (freeTimelinesKeys.size() > 0
						&& unpreparedTimelinesKeys.size() > i) {
					try {
						TIMELINE timeline = TimelineGroup.this
								.getTimeline(freeTimelinesKeys.remove(0));
						timeline.setData(unpreparedTimelinesKeys.get(i));
					} catch (Exception e) {
						LOGGER.error("Error assigning new key "
								+ unpreparedTimelinesKeys.get(i) + " to "
								+ Timeline.class);
					}
				}
			}
		});
		this.disposeTimelines(freeTimelinesKeys);

		final List<INPUT> unpreparedTimelinesKeys_ = new ArrayList<INPUT>();
		for (Object unpreparedTimelineKey : unpreparedTimelinesKeys) {
			unpreparedTimelinesKeys_.add((INPUT) unpreparedTimelineKey);
		}
		return unpreparedTimelinesKeys_;
	}

	/**
	 * Disposes all {@link SelectionTimeline}s that are identified by at least
	 * one of the provided keys.
	 * 
	 * @param freeTimelinesKeys
	 */
	private void disposeTimelines(final List<INPUT> freeTimelinesKeys) {
		ExecUtils.asyncExec(new Runnable() {
			@Override
			public void run() {
				for (INPUT key : freeTimelinesKeys) {
					try {
						TIMELINE timeline = TimelineGroup.this.getTimeline(key);
						if (timeline != null && !timeline.isDisposed()) {
							timeline.dispose();
						}
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

	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

}
