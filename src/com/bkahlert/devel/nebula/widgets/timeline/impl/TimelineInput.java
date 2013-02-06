package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

public class TimelineInput implements ITimelineInput {

	private IOptions options;
	private List<ITimelineBand> bands;
	private Calendar start;
	private Calendar end;

	public TimelineInput(IOptions options, List<ITimelineBand> bands) {
		super();
		this.options = options;
		this.bands = bands;

		List<ITimelineEvent> events = new ArrayList<ITimelineEvent>();
		for (ITimelineBand band : bands)
			events.addAll(band.getEvents());
		Calendar[] range = TimelineEvent.getCalendarRange(events);
		start = range[0];
		end = range[1];
	}

	@Override
	public IOptions getOptions() {
		return this.options;
	}

	@Override
	public List<ITimelineBand> getBands() {
		return this.bands;
	}

	@Override
	public ITimelineBand getBand(int bandNumber) {
		return this.bands.get(bandNumber);
	}

	@Override
	public int getBandCount() {
		return this.bands != null ? this.bands.size() : 0;
	}

	@Override
	public int getEventCount() {
		int eventCount = 0;
		if (this.bands != null) {
			for (ITimelineBand band : this.bands) {
				eventCount += band.getEventCount();
			}
		}
		return eventCount;
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
