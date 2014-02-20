package com.bkahlert.nebula.widgets.timeline.model;

/**
 * A {@link IZoomStep} denotes a possible zoom level.
 * 
 * @author bkahlert
 * 
 */
public interface IZoomStep {

	public float getPixelsPerInterval();

	public Unit getUnit();

	public int getShowLabelEveryUnits();

}