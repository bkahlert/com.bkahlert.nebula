package com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl;

import java.util.List;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProviders;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

public class TimelineProvider<TIMELINE extends IBaseTimeline> implements
		ITimelineProvider<TIMELINE> {
	private ITimelineLabelProvider<TIMELINE> timelineLabelProvider;
	private List<IBandGroupProviders> bandGroupProviders;

	public TimelineProvider(
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider,
			List<IBandGroupProviders> bandGroupProviders) {
		super();
		this.timelineLabelProvider = timelineLabelProvider;
		this.bandGroupProviders = bandGroupProviders;
	}

	@Override
	public ITimelineLabelProvider<TIMELINE> getTimelineLabelProvider() {
		return this.timelineLabelProvider;
	}

	@Override
	public List<IBandGroupProviders> getBandGroupProviders() {
		return this.bandGroupProviders;
	}
}