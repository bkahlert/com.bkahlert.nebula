package com.bkahlert.nebula.widgets.timeline.model;

import java.net.URI;
import java.util.Calendar;

import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.timeline.ITimeline;

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
public interface ITimelineEvent {
	public String getTitle();

	public String getTooltip();

	public URI getIcon();

	public URI getImage();

	public Calendar getStart();

	public Calendar getEnd();

	public String[] getClassNames();

	public RGB[] getColors();

	public boolean isResizable();

	/**
	 * Returns the event's payload. This field is ideal to keep track of an
	 * object this {@link ITimelineEvent} actually presents.
	 * 
	 * @return
	 */
	public Object getPayload();

	// FIXME
	void addClassName(String string);

}
