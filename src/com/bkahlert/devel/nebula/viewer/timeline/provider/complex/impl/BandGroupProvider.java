package com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl;

import com.bkahlert.devel.nebula.viewer.timeline.impl.AbstractTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;

public class BandGroupProvider<TIMELINEGROUPVIEWER extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
		implements
		IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> {

	private ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> contentProvider;
	private ITimelineBandLabelProvider bandLabelProvider;
	private ITimelineEventLabelProvider eventLabelProvider;

	public BandGroupProvider(
			ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> contentProvider,
			ITimelineBandLabelProvider bandLabelProvider,
			ITimelineEventLabelProvider eventLabelProvider) {
		super();
		this.contentProvider = contentProvider;
		this.bandLabelProvider = bandLabelProvider;
		this.eventLabelProvider = eventLabelProvider;
	}

	@Override
	public ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> getContentProvider() {
		return this.contentProvider;
	}

	@Override
	public ITimelineBandLabelProvider getBandLabelProvider() {
		return this.bandLabelProvider;
	}

	@Override
	public ITimelineEventLabelProvider getEventLabelProvider() {
		return this.eventLabelProvider;
	}

}