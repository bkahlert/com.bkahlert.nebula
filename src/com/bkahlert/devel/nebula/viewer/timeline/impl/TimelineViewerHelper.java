package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineEvent;

public class TimelineViewerHelper {
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

	public static List<ITimelineEvent> getEvents(Object[] events,
			ITimelineEventLabelProvider eventLabelProvider) {
		List<ITimelineEvent> timelineEvents = new ArrayList<ITimelineEvent>();

		if (eventLabelProvider != null) {
			for (Object event : events) {
				String title = eventLabelProvider.getTitle(event);
				URI icon = eventLabelProvider.getIcon(event);
				URI image = eventLabelProvider.getIcon(event);
				Calendar start = eventLabelProvider.getStart(event);
				Calendar end = eventLabelProvider.getEnd(event);
				String[] classNames = eventLabelProvider.getClassNames(event);

				ITimelineEvent timelineEvent = new TimelineEvent(title, icon,
						image, start, end, classNames, event);
				timelineEvents.add(timelineEvent);
			}
		}

		return timelineEvents;
	}
}
