package com.bkahlert.devel.nebula.viewer.timeline;

/**
 * Provides label information for the passed timeline band.
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
