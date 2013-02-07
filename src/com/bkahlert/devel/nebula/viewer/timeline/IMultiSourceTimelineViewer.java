package com.bkahlert.devel.nebula.viewer.timeline;

import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timelineGroup.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

/**
 * Interface for {@link ITimelineGroupViewer}.
 * 
 * @author bkahlert
 * 
 */
public interface IMultiSourceTimelineViewer<TIMELINE extends IBaseTimeline>
		extends ITimelineViewer {

	public void setTimelineProvider(ITimelineProvider<TIMELINE> provider);

}