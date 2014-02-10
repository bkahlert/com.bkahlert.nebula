package com.bkahlert.nebula.viewer.timeline.provider.complex;

import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;

/**
 * Instances of this class encapsulate the providers needed to render one or
 * more timeline bands.
 * 
 * @author bkahlert
 * 
 */
public interface IBandGroupProvider<INPUT> {
	public ITimelineContentProvider<INPUT> getContentProvider();

	public ITimelineBandLabelProvider getBandLabelProvider();

	public ITimelineEventLabelProvider getEventLabelProvider();
}