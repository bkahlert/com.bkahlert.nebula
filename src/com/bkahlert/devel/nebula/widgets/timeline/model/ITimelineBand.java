package com.bkahlert.devel.nebula.widgets.timeline.model;

import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;

/**
 * Denotes a band on an {@link IBaseTimeline}.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBand {
	public IOptions getOptions();

	public List<ITimelineEvent> getEvents();

	public ITimelineEvent getEvent(int eventNumber);

	public int getEventCount();

	/**
	 * Start of the first {@link ITimelineEvent}.
	 * 
	 * @return
	 */
	public Calendar getStart();

	/**
	 * End of the last {@link ITimelineEvent}.
	 * 
	 * @return
	 */
	public Calendar getEnd();

}
