package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.impl.SelectionTimeline;

/**
 * Denotes a band on a {@link SelectionTimeline}.
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
