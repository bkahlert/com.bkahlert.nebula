package com.bkahlert.nebula.viewer.timeline.provider.atomic;

import java.util.Calendar;

import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.nebula.widgets.timeline.model.IZoomStep;

/**
 * Instances of this class provide label information for {@link ITimeline}s.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineLabelProvider<TIMELINE extends IBaseTimeline> {

	public String getTitle(TIMELINE timeline);

	public Calendar getCenterStart(TIMELINE timeline);

	public Float getTapeImpreciseOpacity(TIMELINE timeline);

	public Integer getIconWidth(TIMELINE timeline);

	public String[] getBubbleFunction(TIMELINE timeline);

	public IHotZone[] getHotZones(TIMELINE timeline);

	public IDecorator[] getDecorators(TIMELINE timeline);

	public IZoomStep[] getZoomSteps(TIMELINE timeline);

	public Integer getZoomIndex(TIMELINE timeline);

	public Float getTimeZone(TIMELINE timeline);

}
