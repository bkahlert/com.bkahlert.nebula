package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

public class TimelineEvent extends TypedEvent {

	private static final long serialVersionUID = -187925793091438074L;

	/**
	 * the underlying {@link ITimelineEvent} that represented the
	 * {@link #getSource()}
	 */
	public ITimelineEvent timelineEvent;

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
		super(timelineEvent.getPayload());
		this.display = display;
		this.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
		this.widget = (Widget) timeline;
		this.timelineEvent = timelineEvent;
		this.startDate = startDate;
		this.endDate = endDate;
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
		return super.getSource();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " on " + timelineEvent;
	}
}
