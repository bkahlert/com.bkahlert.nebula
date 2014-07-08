package com.bkahlert.nebula.widgets.timeline.model;

import java.util.Calendar;
import java.util.List;

/**
 * FIXME make immutable!!!
 * <p>
 * Input for a timeline.
 * <p>
 * <strong>Important: Instances of this class are immutable and thus are
 * considered static.</strong>
 * 
 * @author bkahlert
 * 
 */
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
