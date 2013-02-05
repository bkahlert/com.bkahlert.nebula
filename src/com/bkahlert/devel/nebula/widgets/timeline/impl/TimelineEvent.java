package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;

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
			if (event.getStart() != null)
				calendars.add(event.getStart());
			if (event.getEnd() != null)
				calendars.add(event.getEnd());
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

	private String title;
	private String icon;
	private String image;
	private Calendar start;
	private Calendar end;
	private List<String> classNames;
	private Object payload;

	public TimelineEvent(String title, String icon, String image,
			Calendar start, Calendar end, List<String> classNames,
			Object payload) {
		super();
		this.title = title;
		this.icon = icon;
		this.image = image;
		this.start = start;
		this.end = end;
		this.classNames = classNames != null ? new ArrayList<String>(classNames)
				: new ArrayList<String>();
		this.payload = payload;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getIcon() {
		return this.icon;
	}

	@Override
	public String getImage() {
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
	public List<String> getClassNames() {
		return this.classNames;
	}

	@Override
	public void addClassName(String className) {
		this.classNames.add(className);
	}

	@Override
	public void removeClassName(String className) {
		this.classNames.remove(className);
	}

	@Override
	public Object getPayload() {
		return this.payload;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return AdapterManager.getDefault().getAdapter(this, adapter);
	}

}
