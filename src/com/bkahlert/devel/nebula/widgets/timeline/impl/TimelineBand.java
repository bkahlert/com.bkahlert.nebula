package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;

public class TimelineBand implements ITimelineBand {

	private IOptions options;
	private List<ITimelineEvent> events;
	private Calendar start;
	private Calendar end;

	public TimelineBand(IOptions options, List<ITimelineEvent> events) {
		super();
		this.options = options;
		this.events = events;

		Calendar[] range = TimelineEvent.getCalendarRange(events);
		start = range[0];
		end = range[1];
	}

	@Override
	public IOptions getOptions() {
		return this.options;
	}

	@Override
	public List<ITimelineEvent> getEvents() {
		return this.events;
	}

	@Override
	public ITimelineEvent getEvent(int eventNumber) {
		return this.events.get(eventNumber);
	}

	@Override
	public int getEventCount() {
		return this.events != null ? this.events.size() : 0;
	}

	@Override
	public Calendar getStart() {
		return this.start;
	}

	@Override
	public Calendar getEnd() {
		return this.end;
	}

}
