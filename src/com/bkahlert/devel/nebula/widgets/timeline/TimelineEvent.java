package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

public class TimelineEvent extends TypedEvent {

	private static final long serialVersionUID = -187925793091438074L;

	/**
	 * the underlying {@link ITimelineEvent}s that represented the
	 * {@link #getSources()}
	 */
	public ITimelineEvent[] timelineEvents;

	/**
	 * If resized this field holds the new start date.
	 */
	public Calendar startDate;

	/**
	 * If resized this field holds the new end date.
	 */
	public Calendar endDate;

	public TimelineEvent(Display display, ITimeline timeline,
			ITimelineEvent timelineEvent, Calendar startDate, Calendar endDate) {
		super(new Object()); // null is not allowed
		this.display = display;
		this.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
		this.widget = (Widget) timeline;
		this.timelineEvents = new ITimelineEvent[] { timelineEvent };
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public TimelineEvent(Display display, ITimeline timeline,
			ITimelineEvent[] timelineEvents, Calendar startDate,
			Calendar endDate) {
		this(display, timeline, timelineEvents != null
				&& timelineEvents.length > 0 ? timelineEvents[0] : null,
				startDate, endDate);
		this.timelineEvents = timelineEvents;
	}

	/**
	 * Returns the object on which this event occurred.<br>
	 * This is typically the object that is represented by the visual component.
	 * 
	 * @return
	 * @see TypedEvent#getSource()
	 */
	@Override
	public Object getSource() {
		return this.timelineEvents != null && this.timelineEvents.length > 0 ? this.timelineEvents[0]
				.getPayload() : null;
	}

	/**
	 * Returns the objects on which this event occurred.<br>
	 * These are typically the objects that are represented by the visual
	 * component.
	 * 
	 * @return
	 * @see TypedEvent#getSource()
	 */
	public Object[] getSources() {
		if (this.timelineEvents == null) {
			return new Object[0];
		}
		Object[] sources = new Object[this.timelineEvents.length];
		for (int i = 0; i < this.timelineEvents.length; i++) {
			sources[i] = this.timelineEvents[i].getPayload();
		}
		return sources;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " on "
				+ StringUtils.join(this.timelineEvents, ", ");
	}
}
