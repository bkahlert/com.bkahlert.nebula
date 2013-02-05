package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Instances of this class denote events on a {@link ITimeline}.<
 * <p>
 * You have two possibilities to make instances of this class link to your
 * business objects:
 * <ol>
 * <li>Implement this interface</li>
 * <li>Pass the business object as the payload and implement an adapter that
 * adapts {@link ITimelineEvent}s to your business object (using the payload).
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineEvent extends IAdaptable {
	public String getTitle();

	public String getIcon();

	public String getImage();

	public Calendar getStart();

	public Calendar getEnd();

	public List<String> getClassNames();

	public void addClassName(String className);

	public void removeClassName(String className);

	/**
	 * Returns the event's payload. This field is ideal to keep track of an
	 * object this {@link ITimelineEvent} actually presents.
	 * 
	 * @return
	 */
	public Object getPayload();

}
