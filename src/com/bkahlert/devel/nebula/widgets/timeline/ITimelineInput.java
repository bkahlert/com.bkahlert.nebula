package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

public interface ITimelineInput {
	public IOptions getOptions();

	public List<ITimelineBand> getBands();

	public ITimelineBand getBand(int bandNumber);

	public int getBandCount();

	public int getEventCount();

	/**
	 * Start of the first {@link ITimelineEvent} contained in the
	 * {@link ITimelineBand}s.
	 * 
	 * @return
	 */
	public Calendar getStart();

	/**
	 * End of the last {@link ITimelineEvent} contained in the
	 * {@link ITimelineBand}s.
	 * 
	 * @return
	 */
	public Calendar getEnd();

}
