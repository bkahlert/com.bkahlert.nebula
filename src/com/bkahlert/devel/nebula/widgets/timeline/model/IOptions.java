package com.bkahlert.devel.nebula.widgets.timeline.model;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;

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

	public String getTitle();

	/**
	 * Sets where on which date to center on startup.
	 * 
	 * @param calendar
	 */
	public void setCenterStart(Calendar calendar);

	public Calendar getCenterStart();

	/**
	 * Sets the opacity of the tape extension of imprecise events.
	 * 
	 * @param opacity
	 *            between 0 and 1
	 */
	public void setTapeImpreciseOpacity(Float opacity);

	public Float getTapeImpreciseOpacity();

	/**
	 * Sets the width of displayed icons.
	 * 
	 * @param iconWidth
	 */
	public void setIconWidth(Integer iconWidth);

	public Integer getIconWidth();

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

	public String[] getBubbleFunction();

	/**
	 * Sets the time spans you want to be zoomed in.
	 */
	public void setHotZones(IHotZone[] hotZones);

	public IHotZone[] getHotZones();

	/**
	 * Sets the {@link IDecorator}s that are always displayed.
	 * 
	 * @param decorators
	 */
	public void setPermanentDecorators(IDecorator[] decorators);

	public IDecorator[] getPermanentDecorators();

	/**
	 * Sets the {@link IDecorator}s that are only displayed when the timeline is
	 * loaded.
	 * <p>
	 * The decorators become overwritten when the user calls
	 * {@link IBaseTimeline#setDecorators(IDecorator[])}.
	 * 
	 * @param decorators
	 */
	public void setDecorators(IDecorator[] decorators);

	public IDecorator[] getDecorators();

	/**
	 * Sets the {@link TimeZone} to be used for browsing.
	 * 
	 * @param offset
	 *            e.g. 2 for 02:00 or -5.5 for -05:30
	 */
	public void setTimeZone(Float offset);

	public Float getTimeZone();

	/**
	 * Indicates whether {@link ITimelineEvent}s should also be displayed in the
	 * overview {@link ITimelineBand}.
	 * 
	 * @param showInOverviewBands
	 */
	public void setShowInOverviewBands(Boolean showInOverviewBands);

	public Boolean getShowInOverviewBands();

	/**
	 * Indicates a {@link ITimelineBand}'s ratio it wants to occupy of the total
	 * display height.
	 * 
	 * @param ratio
	 *            (from 0 to 1)
	 */
	public void setRatio(Float ratio);

	public Float getRatio();

	/**
	 * Returns the possible levels the timeline's custom bands can zoom to.
	 * 
	 * @return
	 */
	public IZoomStep[] getZoomSteps();

	public void setZoomSteps(IZoomStep[] zoomSteps);

	/**
	 * Returns the position of the {@link IZoomStep} to use on load.
	 * 
	 * @return
	 */
	public Integer getZoomIndex();

	public void setZoomIndex(Integer zoomIndex);

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
