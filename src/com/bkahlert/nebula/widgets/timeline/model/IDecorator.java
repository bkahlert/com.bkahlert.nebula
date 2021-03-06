package com.bkahlert.nebula.widgets.timeline.model;

/**
 * A {@link IDecorator} denotes a time span on a time line that receives a
 * different background color when rendered. You may optionally provide labels
 * that will show up next to the time span's start and end.
 * 
 * @author bkahlert
 * 
 */
public interface IDecorator {

	public String getStartDate();

	public String getEndDate();

	public String getStartLabel();

	public String getEndLabel();

}