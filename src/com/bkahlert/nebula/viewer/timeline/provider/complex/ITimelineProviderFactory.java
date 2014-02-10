package com.bkahlert.nebula.viewer.timeline.provider.complex;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

/**
 * Instances of this class create {@link ITimelineProvider} objects.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineProviderFactory<TIMELINE extends IBaseTimeline, INPUT> {
	public ITimelineProvider<TIMELINE, INPUT> createTimelineProvider();
}