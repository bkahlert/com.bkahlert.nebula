package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.net.URI;
import java.util.Calendar;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

/**
 * This class provides some helper methods for generating {@link IOptions} and
 * {@link ITimelineEvent}s.
 * 
 * @author bkahlert
 * 
 */
public class TimelineViewerHelper {

	/**
	 * Generates the options for a {@link ITimeline} using a given
	 * {@link ITimelineLabelProvider}.
	 * 
	 * @param timelineLabelProvider
	 * @return
	 */
	public static IOptions getTimelineOptions(
			ITimelineLabelProvider timelineLabelProvider) {
		IOptions options = new Options();
		if (timelineLabelProvider != null) {
			options.setTitle(timelineLabelProvider.getTitle());
			options.setCenterStart(timelineLabelProvider.getCenterStart());
			options.setTapeImpreciseOpacity(timelineLabelProvider
					.getTapeImpreciseOpacity());
			options.setIconWidth(timelineLabelProvider.getIconWidth());

			String[] bubbleFunction = timelineLabelProvider.getBubbleFunction();
			String functionName = bubbleFunction != null
					&& bubbleFunction.length > 0 ? bubbleFunction[0] : null;
			String functionField = bubbleFunction != null
					&& bubbleFunction.length > 1 ? bubbleFunction[1] : null;
			options.setBubbleFunction(functionName, functionField);

			options.setHotZones(timelineLabelProvider.getHotZones());
			options.setDecorators(timelineLabelProvider.getDecorators());
			options.setTimeZone(timelineLabelProvider.getTimeZone());
		}
		return options;
	}

	/**
	 * Generates the options for a specified band using a given
	 * {@link ITimelineBandLabelProvider}.
	 * 
	 * @param band
	 *            the object representing the band
	 * @param bandLabelProvider
	 *            the provider to ask
	 * @return
	 */
	public static IOptions getBandOptions(Object band,
			ITimelineBandLabelProvider bandLabelProvider) {
		IOptions bandOptions = new Options();

		if (bandLabelProvider != null) {
			String title = bandLabelProvider.getTitle(band);
			if (title != null)
				bandOptions.setTitle(title);

			Boolean isShowInOverviewBands = bandLabelProvider
					.isShowInOverviewBands(band);
			if (isShowInOverviewBands != null)
				bandOptions.setShowInOverviewBands(isShowInOverviewBands);

			Float ratio = bandLabelProvider.getRatio(band);
			if (ratio != null)
				bandOptions.setRatio(ratio);
		}

		return bandOptions;
	}

	/**
	 * Takes a value object and wraps it in a {@link ITimelineEvent} using the
	 * given {@link ITimelineEventLabelProvider}.
	 * 
	 * @param events
	 * @param eventLabelProvider
	 * @param additionalClassName
	 * @return
	 */
	public static ITimelineEvent getEvent(Object event,
			ITimelineEventLabelProvider eventLabelProvider) {
		if (eventLabelProvider == null)
			return null;

		String title = eventLabelProvider.getTitle(event);
		URI icon = eventLabelProvider.getIcon(event);
		URI image = eventLabelProvider.getIcon(event);
		Calendar start = eventLabelProvider.getStart(event);
		Calendar end = eventLabelProvider.getEnd(event);
		String[] classNames = eventLabelProvider.getClassNames(event);

		ITimelineEvent timelineEvent = new TimelineEvent(title, icon, image,
				start, end, classNames, event);
		return timelineEvent;
	}
}
