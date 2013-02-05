package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Map;

/**
 * Instances of this class can be used as options for {@link ITimelineInput}s or
 * {@link ITimelineBand}.
 * 
 * @author bkahlert
 * 
 */
public interface IOptions extends Map<String, Object> {

	/**
	 * Sets the title field.
	 * 
	 * @param title
	 */
	public void setTitle(String title);

	/**
	 * Indicates whether {@link ITimelineEvent}s should also be displayed in the
	 * overview {@link ITimelineBand}.
	 * 
	 * @param showInOverviewBands
	 */
	public void setShowInOverviewBands(boolean showInOverviewBands);

	/**
	 * Indicates a {@link ITimelineBand}'s ratio it wants to occupy of the total
	 * display height.
	 * 
	 * @param ratio
	 *            (from 0 to 1)
	 */
	public void setRatio(float ratio);
}
