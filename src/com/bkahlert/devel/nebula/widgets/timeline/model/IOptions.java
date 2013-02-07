package com.bkahlert.devel.nebula.widgets.timeline.model;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;


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
	 * Sets where on which date to center on startup.
	 * 
	 * @param calendar
	 */
	public void setCenterStart(Calendar calendar);

	/**
	 * Sets the opacity of the tape extension of imprecise events.
	 * 
	 * @param opacity
	 *            between 0 and 1
	 */
	public void setTapeImpreciseOpacity(Float opacity);

	/**
	 * Sets the width of displayed icons.
	 * 
	 * @param iconWidth
	 */
	public void setIconWidth(Integer iconWidth);

	/**
	 * Sets the function to be used when the user clicks on an event.
	 * 
	 * @param functionName
	 *            the name of the function
	 * @param functionField
	 *            the name of the events field to be passed as the first
	 *            argument of the function
	 */
	public void setBubbleFunction(String functionName, String functionField);

	/**
	 * Sets the time spans you want to be zoomed in.
	 */
	public void setHotZones(IHotZone[] hotZones);

	/**
	 * Sets the {@link IDecorator}s that you always be displayed.
	 * 
	 * @param decorators
	 */
	public void setDecorators(IDecorator[] decorators);

	/**
	 * Sets the {@link TimeZone} to be used for browsing.
	 * 
	 * @param offset
	 *            e.g. 2 for 02:00 or -5.5 for -05:30
	 */
	public void setTimeZone(Float offset);

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

	/*
	 * TODO fix timeline javascript to support timeline_start and timeline_end
	 */
	// if (dateRange.getStartDate() != null)
	// options.put("timeline_start",
	// TimelineJsonGenerator.formatDate(dateRange.getStartDate()));
	// if (dateRange.getEndDate() != null)
	// options.put("timeline_end",
	// TimelineJsonGenerator.formatDate(dateRange.getEndDate()));
}
