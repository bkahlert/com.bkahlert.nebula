package com.bkahlert.devel.nebula.viewer.timelineGroup;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

/**
 * Interface for {@link ITimelineViewer} using multiple sets of label providers.
 * 
 * @author bkahlert
 * 
 */
public interface IMultiSourceTimelineGroupViewer<TIMELINE extends IBaseTimeline>
		extends ITimelineViewer {

	public void setTimelineProviderFactory(
			ITimelineProviderFactory<TIMELINE> timelineProviderFactory);

}