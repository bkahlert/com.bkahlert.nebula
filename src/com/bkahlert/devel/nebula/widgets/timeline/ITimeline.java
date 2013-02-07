package com.bkahlert.devel.nebula.widgets.timeline;

/**
 * This interface extends the {@link IBaseTimeline} by two functionalities:
 * <ol>
 * <li>Resolve the next and previous event given a event</li>
 * <li>Listener support</li>
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public interface ITimeline extends IBaseTimeline {

	/**
	 * Returns the object attached to the event that is the closest one to the
	 * given event (starting later).
	 * 
	 * @param event
	 * @return
	 */
	public Object getSuccessor(Object event);

	/**
	 * Returns the object attached to the event that is the closest one to the
	 * given event (starting earlier).
	 * 
	 * @param event
	 * @return
	 */
	public Object getPredecessor(Object event);

	public void addTimelineListener(ITimelineListener timelineListener);

	public void removeTimelineListener(ITimelineListener timelineListener);

}