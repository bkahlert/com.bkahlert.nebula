package com.bkahlert.devel.nebula.viewer.timeline.provider.atomic;

import java.net.URI;
import java.util.Calendar;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

/**
 * Provides label information for the passed {@link ITimeline} event represented
 * by a value object.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineEventLabelProvider {
	public String getTitle(Object event);

	public URI getIcon(Object event);

	public URI getImage(Object event);

	public Calendar getStart(Object event);

	public Calendar getEnd(Object event);

	public RGB[] getColors(Object event);

	public boolean isResizable(Object event);

	public String[] getClassNames(Object event);
}
