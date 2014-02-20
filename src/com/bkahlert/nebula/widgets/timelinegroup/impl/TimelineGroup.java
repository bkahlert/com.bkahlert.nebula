package com.bkahlert.nebula.widgets.timelinegroup.impl;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.nebula.widgets.timeline.TimelineEvent;

/**
 * This widget display one or more {@code TIMELINE}s.
 * 
 * @author bkahlert
 */
public class TimelineGroup<TIMELINE extends ITimeline, INPUT> extends
		BaseTimelineGroup<TIMELINE, INPUT> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(TimelineGroup.class);

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

		@Override
		public void selected(TimelineEvent event) {
			Object[] listeners = TimelineGroup.this.timelineListeners
					.getListeners();
			for (Object listener : listeners) {
				((ITimelineListener) listener).selected(event);
			}
		}
	};

	public TimelineGroup(Composite parent, int style,
			ITimelineFactory<TIMELINE> timelineFactory) {
		super(parent, style, timelineFactory);
	}

	@Override
	public TIMELINE createTimeline() {
		final TIMELINE timeline = super.createTimeline();
		timeline.addTimelineListener(this.timelineListenerDelegate);
		timeline.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				timeline.addTimelineListener(TimelineGroup.this.timelineListenerDelegate);
			}
		});
		return timeline;
	}

	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

}
