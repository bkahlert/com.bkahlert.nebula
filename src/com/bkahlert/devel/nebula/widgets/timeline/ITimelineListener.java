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
	 * Is called if an {@link TimelineEvent} resize process is started.
	 * 
	 * @param event
	 */
	public void resizeStarted(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} is currently resized.
	 * 
	 * @param event
	 */
	public void resizing(TimelineEvent event);

	/**
	 * Is called if an {@link TimelineEvent} is resized. (meaning the process
	 * has finished)
	 * 
	 * @param event
	 */
	public void resized(TimelineEvent event);

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

	/**
	 * Is called if {@link TimelineEvent}s were (un)selected.
	 * 
	 * @param event
	 */
	public void selected(TimelineEvent event);
}