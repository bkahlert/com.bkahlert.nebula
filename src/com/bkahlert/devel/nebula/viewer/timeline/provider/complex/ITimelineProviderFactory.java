package com.bkahlert.devel.nebula.viewer.timeline.provider.complex;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

/**
 * Instances of this class create {@link ITimelineProvider} objects.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineProviderFactory<TIMELINE extends IBaseTimeline> {
	public ITimelineProvider<TIMELINE> createTimelineProvider();
}