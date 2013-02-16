package com.bkahlert.devel.nebula.viewer.timeline.provider.complex;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;

/**
 * Instances of this class encapsulate the providers needed to render one or
 * more timeline bands.
 * 
 * @author bkahlert
 * 
 */
public interface IBandGroupProvider {
	public ITimelineContentProvider getContentProvider();

	public ITimelineBandLabelProvider getBandLabelProvider();

	public ITimelineEventLabelProvider getEventLabelProvider();
}