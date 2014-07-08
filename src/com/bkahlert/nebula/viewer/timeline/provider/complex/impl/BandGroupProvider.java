package com.bkahlert.nebula.viewer.timeline.provider.complex.impl;

import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.IBandGroupProvider;

public class BandGroupProvider<INPUT> implements IBandGroupProvider<INPUT> {

	private final ITimelineContentProvider<INPUT> contentProvider;
	private final ITimelineBandLabelProvider bandLabelProvider;
	private final ITimelineEventLabelProvider eventLabelProvider;

	public BandGroupProvider(ITimelineContentProvider<INPUT> contentProvider,
			ITimelineBandLabelProvider bandLabelProvider,
			ITimelineEventLabelProvider eventLabelProvider) {
		super();
		this.contentProvider = contentProvider;
		this.bandLabelProvider = bandLabelProvider;
		this.eventLabelProvider = eventLabelProvider;
	}

	@Override
	public ITimelineContentProvider<INPUT> getContentProvider() {
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