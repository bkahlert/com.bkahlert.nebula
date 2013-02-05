package com.bkahlert.devel.nebula.utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CalendarUtils {

	private static Calendar[] getSortedCalendars(List<Calendar> calendars) {
		Calendar[] calendarArray = calendars.toArray(new Calendar[0]);
		Arrays.sort(calendarArray);
		return calendarArray;
	}

	public static Calendar[] getRange(List<Calendar> calendars) {
		Calendar[] sortedCalendars = getSortedCalendars(calendars);
		switch (sortedCalendars.length) {
		case 0:
			return null;
		case 1:
			return new Calendar[] { sortedCalendars[0] };
		default:
			return new Calendar[] { sortedCalendars[0],
					sortedCalendars[sortedCalendars.length - 1] };
		}
	}

	public static Calendar getEarliestCalendar(List<Calendar> calendars) {
		Calendar[] sortedCalendars = getSortedCalendars(calendars);
		return sortedCalendars.length > 0 ? sortedCalendars[0] : null;
	}

	public static Calendar getLatestCalendar(List<Calendar> calendars) {
		Calendar[] sortedCalendars = getSortedCalendars(calendars);
		return sortedCalendars.length > 0 ? sortedCalendars[sortedCalendars.length - 1]
				: null;
	}

}
