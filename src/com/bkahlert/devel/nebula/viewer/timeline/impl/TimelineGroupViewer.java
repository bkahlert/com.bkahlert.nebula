package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.junit.Assert;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;

/**
 * This implementation supports to set decorators and keeps them, the focused
 * item, the current scroll position and the zoom index unchanged if a
 * {@link ITimeline} is refreshed.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINEGROUP>
 * @param <TIMELINE>
 */
public class TimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
		extends MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT> {

	private static final Logger LOGGER = Logger
			.getLogger(TimelineGroupViewer.class);

	public TimelineGroupViewer(
			TIMELINEGROUP timelineGroup,
			ITimelineProviderFactory<MinimalTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP, TIMELINE, INPUT> timelineProviderFactory) {
		super(timelineGroup, timelineProviderFactory);
	}

	/**
	 * Highlights the given date.
	 * 
	 * @param groupedDecorators
	 * @param progressMonitor
	 */
	public void setCenterVisibleDate(final Map<Object, Calendar> calendars,
			IProgressMonitor monitor) {
		Assert.assertNotNull(calendars);

		SubMonitor subMonitor = SubMonitor.convert(monitor, calendars.keySet()
				.size());

		for (final Object key : calendars.keySet()) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final TIMELINE timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return TimelineGroupViewer.this.getTimeline(key);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving timeline for " + key);
				continue;
			}
			if (timeline == null) {
				LOGGER.warn("Timeline does not exist anymore for " + key);
				continue;
			}

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					Calendar calendar = calendars.get(key);
					if (calendar != null) {
						timeline.setCenterVisibleDate(calendar);
					}
				}
			});

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		subMonitor.done();
	}

	/**
	 * Highlights the given date ranges in the timelines.
	 * 
	 * @param groupedDecorators
	 * @param progressMonitor
	 */
	public void setDecorators(
			final Map<Object, IDecorator[]> groupedDecorators,
			IProgressMonitor monitor) {

		SubMonitor subMonitor = SubMonitor.convert(monitor, groupedDecorators
				.keySet().size());

		for (final Object key : groupedDecorators.keySet()) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final TIMELINE timeline;
			try {
				timeline = ExecutorUtil.asyncExec(new Callable<TIMELINE>() {
					@Override
					public TIMELINE call() throws Exception {
						return TimelineGroupViewer.this.getTimeline(key);
					}
				}).get();
			} catch (Exception e) {
				LOGGER.error("Error retrieving timeline for " + key);
				continue;
			}
			if (timeline == null) {
				LOGGER.warn("Timeline does not exist anymore for " + key);
				continue;
			}

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			ExecutorUtil.asyncExec(new Runnable() {
				@Override
				public void run() {
					IDecorator[] decorators = groupedDecorators.get(key);
					if (decorators == null || decorators.length == 0) {
						timeline.setDecorators(new IDecorator[0]);
					} else {
						timeline.setDecorators(decorators);
					}
				}
			});

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		subMonitor.done();
	}

	@Override
	protected void postProcess(Object businessObject, ITimelineEvent event,
			boolean inputIsNew) {
		if (inputIsNew) {
			return;
		}

		/*
		 * Preserves focused item
		 */
		if (SelectionUtils.getAdaptableObjects(this.getSelection(),
				Object.class).contains(businessObject)) {
			event.addClassName("focus");
		}
	}

	@Override
	protected void postProcess(final TIMELINE timeline, ITimelineInput input,
			boolean inputIsNew) {
		if (inputIsNew) {
			return;
		}

		// TODO implement mememto that saves all information below

		// center data
		Calendar centerVisibleDate = null;
		try {
			centerVisibleDate = ExecutorUtil.syncExec(new Callable<Calendar>() {
				@Override
				public Calendar call() throws Exception {
					return timeline.getCenterVisibleDate();
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error retrieving the currently centered time", e);
		}

		// zoom index
		Integer zoomIndex = null;
		try {
			zoomIndex = ExecutorUtil.syncExec(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return timeline.getZoomIndex();
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error retrieving the current zoom index", e);
		}

		// on first start we don't want to override the original set date
		if (centerVisibleDate != null) {
			input.getOptions().setCenterStart(centerVisibleDate);
			input.getOptions().setZoomIndex(zoomIndex);
		}

		input.getOptions().setDecorators(timeline.getDecorators());
	}

}
