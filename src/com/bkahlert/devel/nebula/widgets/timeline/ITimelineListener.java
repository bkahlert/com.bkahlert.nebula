package com.bkahlert.devel.nebula.widgets.timeline;

/**
 * This listener notifies about events that occured on the hosting
 * {@link ITimeline}.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineListener {
	/**
	 * Is called if an {@link TimelineEvent} was clicked with the left button.
	 * 
	 * @param event
	 */
	public void clicked(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} was clicked.
	 * 
	 * @param event
	 */
	public void middleClicked(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} was clicked.
	 * 
	 * @param event
	 */
	public void rightClicked(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} was double clicked.
	 * <p>
	 * Please note that the the clicks will also trigger the click event.
	 * 
	 * @param event
	 */
	public void doubleClicked(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} was hovered in, meaning the mouse
	 * moved over it.
	 * 
	 * @param event
	 */
	public void hoveredIn(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} was hovered out, meaning the mouse
	 * moved away from it.
	 * 
	 * @param event
	 */
	public void hoveredOut(TimelineEvent event);
}