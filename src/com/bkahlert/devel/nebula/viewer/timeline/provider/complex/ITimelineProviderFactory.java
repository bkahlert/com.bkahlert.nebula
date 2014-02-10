package com.bkahlert.devel.nebula.viewer.timeline.provider.complex;

import com.bkahlert.devel.nebula.viewer.timeline.impl.AbstractTimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * Instances of this class create {@link ITimelineProvider} objects.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineProviderFactory<TIMELINEGROUPVIEWER extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT> {
	public ITimelineProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> createTimelineProvider();
}