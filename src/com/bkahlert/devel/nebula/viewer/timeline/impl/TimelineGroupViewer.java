package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.junit.Assert;

import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.rcp.selectionUtils.SelectionUtils;
import com.bkahlert.nebula.datetime.CalendarRange;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

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
	public void setCenterVisibleDate(final Map<INPUT, Calendar> calendars,
			IProgressMonitor monitor) {
		Assert.assertNotNull(calendars);

		SubMonitor subMonitor = SubMonitor.convert(monitor, calendars.keySet()
				.size());

		for (final INPUT key : calendars.keySet()) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final TIMELINE timeline = this.getTimeline(key);
			if (timeline == null) {
				LOGGER.warn("Timeline does not exist anymore for " + key);
				continue;
			}

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			Calendar calendar = calendars.get(key);
			if (calendar != null) {
				timeline.setCenterVisibleDate(calendar);
			}

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
	public void setDecorators(final Map<INPUT, IDecorator[]> groupedDecorators,
			IProgressMonitor monitor) {

		SubMonitor subMonitor = SubMonitor.convert(monitor, groupedDecorators
				.keySet().size());

		for (final INPUT key : groupedDecorators.keySet()) {
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final TIMELINE timeline = this.getTimeline(key);
			if (timeline == null) {
				LOGGER.warn("Timeline does not exist anymore for " + key);
				continue;
			}

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			IDecorator[] decorators = groupedDecorators.get(key);
			if (decorators == null || decorators.length == 0) {
				timeline.setDecorators(new IDecorator[0]);
			} else {
				timeline.setDecorators(decorators);
			}

			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		subMonitor.done();
	}

	/**
	 * Highlights the given date ranges.
	 * 
	 * @param groupedRanges
	 * @param progressMonitor
	 */
	public void highlight(Map<INPUT, CalendarRange[]> groupedRanges,
			IProgressMonitor monitor) {

		Map<INPUT, IDecorator[]> groupedDecorators = new HashMap<INPUT, IDecorator[]>();

		for (final INPUT key : groupedRanges.keySet()) {

			final CalendarRange[] dateRanges = groupedRanges.get(key);

			List<IDecorator> decorators = new ArrayList<IDecorator>(
					dateRanges.length);
			for (CalendarRange dateRange : dateRanges) {
				if (dateRange.getStartDate() == null
						&& dateRange.getEndDate() == null) {
					continue;
				}
				decorators.add(new Decorator(
						dateRange.getStartDate() != null ? dateRange
								.getStartDate() : null,
						dateRange.getEndDate() != null ? dateRange.getEndDate()
								: null));
			}
			groupedDecorators.put(key, decorators.toArray(new IDecorator[0]));
		}
		this.setDecorators(groupedDecorators, monitor);
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
		Future<Calendar> centerVisibleDate = timeline.getCenterVisibleDate();

		// zoom index
		Future<Integer> zoomIndex = timeline.getZoomIndex();

		// on first start we don't want to override the original set date
		try {
			if (centerVisibleDate.get() != null) {
				input.getOptions().setCenterStart(centerVisibleDate.get());
				input.getOptions().setZoomIndex(zoomIndex.get());
			}
		} catch (Exception e) {
			LOGGER.error(
					"Error restoring center start and zoom index of timeline",
					e);
		}

		input.getOptions().setDecorators(timeline.getDecorators());
	}

}
