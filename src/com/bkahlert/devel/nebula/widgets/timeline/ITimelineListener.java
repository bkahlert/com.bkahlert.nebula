package com.bkahlert.devel.nebula.widgets.timeline;

/**
 * FIXME add Mouse down etc. für drag support
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineListener {
	/**
	 * Is called if an {@link ITimelineEvent} was clicked with the left button.
	 * 
	 * @param timelineEvent
	 */
	public void clicked(ITimelineEvent timelineEvent);

	/**
	 * Is called if an {@link ITimelineEvent} was clicked.
	 * 
	 * @param timelineEvent
	 */
	public void middleClicked(ITimelineEvent timelineEvent);

	/**
	 * Is called if an {@link ITimelineEvent} was clicked.
	 * 
	 * @param timelineEvent
	 */
	public void rightClicked(ITimelineEvent timelineEvent);

	/**
	 * Is called if an {@link ITimelineEvent} was double clicked.
	 * <p>
	 * Please note that the the clicks will also trigger the click event.
	 * 
	 * @param timelineEvent
	 */
	public void doubleClicked(ITimelineEvent timelineEvent);

	/**
	 * Is called if an {@link ITimelineEvent} was hovered in, meaning the mouse
	 * moved over it.
	 * 
	 * @param timelineEvent
	 */
	public void hoveredIn(ITimelineEvent timelineEvent);

	/**
	 * Is called if an {@link ITimelineEvent} was hovered out, meaning the mouse
	 * moved away from it.
	 * 
	 * @param timelineEvent
	 */
	public void hoveredOut(ITimelineEvent timelineEvent);
}