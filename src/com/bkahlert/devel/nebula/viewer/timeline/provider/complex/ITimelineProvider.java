package com.bkahlert.devel.nebula.viewer.timeline.provider.complex;

import java.util.List;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

/**
 * Instances of this class encapsulate the providers needed to render one
 * timeline.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineProvider<TIMELINE extends IBaseTimeline> {
	public ITimelineLabelProvider<TIMELINE> getTimelineLabelProvider();

	public List<IBandGroupProviders> getBandGroupProviders();
}