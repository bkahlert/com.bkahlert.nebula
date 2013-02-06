package com.bkahlert.devel.nebula.viewer.timeline;

import java.util.Calendar;

import com.bkahlert.devel.nebula.widgets.timeline.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

/**
 * Instances of this class provide label information for {@link ITimeline}s.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineLabelProvider {

	public String getTitle();

	public Calendar getCenterStart();

	public Float getTapeImpreciseOpacity();

	public Integer getIconWidth();

	public String[] getBubbleFunction();

	public IHotZone[] getHotZones();

	public IDecorator[] getDecorators();

	public Float getTimeZone();

}
