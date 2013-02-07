package com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProviders;

public class BandGroupProviders implements IBandGroupProviders {

	private ITimelineContentProvider contentProvider;
	private ITimelineBandLabelProvider bandLabelProvider;
	private ITimelineEventLabelProvider eventLabelProvider;

	public BandGroupProviders(ITimelineContentProvider contentProvider,
			ITimelineBandLabelProvider bandLabelProvider,
			ITimelineEventLabelProvider eventLabelProvider) {
		super();
		this.contentProvider = contentProvider;
		this.bandLabelProvider = bandLabelProvider;
		this.eventLabelProvider = eventLabelProvider;
	}

	@Override
	public ITimelineContentProvider getContentProvider() {
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