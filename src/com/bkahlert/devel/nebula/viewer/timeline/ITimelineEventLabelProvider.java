package com.bkahlert.devel.nebula.viewer.timeline;

import java.net.URI;
import java.util.Calendar;

/**
 * Provides label information for the passed timeline event.
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

	public String[] getClassNames(Object event);
}
