package com.bkahlert.nebula.widgets.timeline.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;

public class TimelineEvent implements ITimelineEvent {

	/**
	 * Calculates the earliest and latest {@link Calendar} in all provided
	 * {@link ITimelineEvent}s.
	 * 
	 * @return an array of two elements; the first one is the earliest and the
	 *         last one the latest {@link Calendar}s
	 */
	static Calendar[] getCalendarRange(List<ITimelineEvent> events) {
		List<Calendar> calendars = new ArrayList<Calendar>();
		for (ITimelineEvent event : events) {
			if (event.getStart() != null) {
				calendars.add(event.getStart());
			}
			if (event.getEnd() != null) {
				calendars.add(event.getEnd());
			}
		}
		Calendar[] range = CalendarUtils.getRange(calendars);
		Calendar start, end;
		if (range == null || range.length == 0) {
			start = null;
			end = null;
		} else {
			start = range[0];
			end = range[range.length - 1];
		}
		return new Calendar[] { start, end };
	}

	private final String title;
	private final String tooltip;
	private final URI icon;
	private final URI image;
	private final Calendar start;
	private final Calendar end;
	private final RGB[] colors;
	private final boolean resizable;
	private String[] classNames;
	private final Object payload;

	public TimelineEvent(String title, String tooltip, URI icon, URI image,
			Calendar start, Calendar end, RGB[] colors, boolean resizable,
			String[] classNames, Object payload) {
		super();
		if (start == null && end == null) {
			throw new IllegalArgumentException(
					"Event must have at least a start or an end date");
		}
		if (payload == null) {
			throw new IllegalArgumentException(
					"Event must have a payload != null");
		}
		this.title = title;
		this.tooltip = tooltip;
		this.icon = icon;
		this.image = image;
		this.start = start;
		this.end = end;
		this.colors = colors;
		this.resizable = resizable;
		this.classNames = classNames != null ? classNames : new String[0];
		this.payload = payload;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getTooltip() {
		return this.tooltip;
	}

	@Override
	public URI getIcon() {
		return this.icon;
	}

	@Override
	public URI getImage() {
		return this.image;
	}

	@Override
	public Calendar getStart() {
		return this.start;
	}

	@Override
	public Calendar getEnd() {
		return this.end;
	}

	@Override
	public RGB[] getColors() {
		return this.colors;
	}

	@Override
	public boolean isResizable() {
		return this.resizable;
	}

	@Override
	public String[] getClassNames() {
		return this.classNames;
	}

	// FIXME
	@Override
	public void addClassName(String string) {
		List<String> classNames = new ArrayList<String>(
				Arrays.asList(this.classNames));
		classNames.add(string);
		this.classNames = classNames.toArray(new String[0]);
	}

	@Override
	public Object getPayload() {
		return this.payload;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": " + this.getTitle()
				+ " - (" + this.getPayload() + ")";
	}

}
