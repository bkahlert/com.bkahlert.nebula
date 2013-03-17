package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.net.URI;
import java.util.Calendar;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

/**
 * This class provides some helper methods for generating {@link IOptions} and
 * {@link ITimelineEvent}s.
 * 
 * @author bkahlert
 * 
 */
public class TimelineViewerHelper<TIMELINE extends IBaseTimeline> {

	/**
	 * Generates the options for a {@link ITimeline} using a given
	 * {@link ITimelineLabelProvider}.
	 * 
	 * @param timelineLabelProvider
	 * @return
	 */
	public static <TIMELINE extends IBaseTimeline> IOptions getTimelineOptions(
			TIMELINE timeline,
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider) {
		IOptions options = new Options();
		if (timelineLabelProvider != null) {
			options.setTitle(timelineLabelProvider.getTitle(timeline));
			options.setCenterStart(timelineLabelProvider
					.getCenterStart(timeline));
			options.setTapeImpreciseOpacity(timelineLabelProvider
					.getTapeImpreciseOpacity(timeline));
			options.setIconWidth(timelineLabelProvider.getIconWidth(timeline));

			String[] bubbleFunction = timelineLabelProvider
					.getBubbleFunction(timeline);
			String functionName = bubbleFunction != null
					&& bubbleFunction.length > 0 ? bubbleFunction[0] : null;
			String functionField = bubbleFunction != null
					&& bubbleFunction.length > 1 ? bubbleFunction[1] : null;
			options.setBubbleFunction(functionName, functionField);

			options.setHotZones(timelineLabelProvider.getHotZones(timeline));
			options.setPermanentDecorators(timelineLabelProvider
					.getDecorators(timeline));
			options.setZoomSteps(timelineLabelProvider.getZoomSteps(timeline));
			options.setZoomIndex(timelineLabelProvider.getZoomIndex());
			options.setTimeZone(timelineLabelProvider.getTimeZone(timeline));
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
			if (title != null) {
				bandOptions.setTitle(title);
			}

			Boolean isShowInOverviewBands = bandLabelProvider
					.isShowInOverviewBands(band);
			if (isShowInOverviewBands != null) {
				bandOptions.setShowInOverviewBands(isShowInOverviewBands);
			}

			Float ratio = bandLabelProvider.getRatio(band);
			if (ratio != null) {
				bandOptions.setRatio(ratio);
			}
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
		if (eventLabelProvider == null) {
			return null;
		}

		String title = eventLabelProvider.getTitle(event);
		String tooltip = eventLabelProvider.getTooltip(event);
		URI icon = eventLabelProvider.getIcon(event);
		URI image = eventLabelProvider.getIcon(event);
		Calendar start = eventLabelProvider.getStart(event);
		Calendar end = eventLabelProvider.getEnd(event);
		RGB[] colors = eventLabelProvider.getColors(event);
		boolean resizable = eventLabelProvider.isResizable(event);
		String[] classNames = eventLabelProvider.getClassNames(event);

		ITimelineEvent timelineEvent = new TimelineEvent(title, tooltip, icon,
				image, start, end, colors, resizable, classNames, event);
		return timelineEvent;
	}
}
