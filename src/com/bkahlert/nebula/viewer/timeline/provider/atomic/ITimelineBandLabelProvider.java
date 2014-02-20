package com.bkahlert.nebula.viewer.timeline.provider.atomic;

import com.bkahlert.nebula.widgets.timeline.ITimeline;

/**
 * Provides label information for the passed {@link ITimeline} band defined by a
 * value object.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineBandLabelProvider {
	/**
	 * Returns the band's title.
	 * 
	 * @param band
	 * @return
	 */
	public String getTitle(Object band);

	/**
	 * Returns if the events of this band should be shown in the overview bands.
	 * 
	 * @param band
	 * @return
	 */
	public Boolean isShowInOverviewBands(Object band);

	/**
	 * Returns the percentage this band occupies.
	 * 
	 * @param band
	 * @return null if unspecified
	 */
	public Float getRatio(Object band);
}
