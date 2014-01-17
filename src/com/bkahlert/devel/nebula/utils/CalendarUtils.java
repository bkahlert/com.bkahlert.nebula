package com.bkahlert.devel.nebula.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

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

	/**
	 * Converts a {@link Calendar} object to its ISO 8601 representation.
	 * {@link Date}s are not supported since they don't provide any
	 * {@link TimeZone} information.
	 * <p>
	 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
	 * 1984-05-15T14:30:00.000+02:00.
	 * 
	 * @param calendar
	 * @return
	 */
	public static String toISO8601(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SZ");
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2) + ":"
				+ missingDots.substring(missingDots.length() - 2);
	}

	public static String toISO8601FileSystemCompatible(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH-mm-ss.SZ");
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2)
				+ missingDots.substring(missingDots.length() - 2);
	}

	public static Calendar fromISO8601(String lexicalRepresentation)
			throws IllegalArgumentException {
		try {
			String[] tries = new String[] { lexicalRepresentation,
					cleanISO8601(lexicalRepresentation) };

			IllegalArgumentException ex = null;
			for (String trie : tries) {
				try {
					return DatatypeFactory.newInstance()
							.newXMLGregorianCalendar(trie)
							.toGregorianCalendar();
				} catch (IllegalArgumentException e) {
					if (ex == null) {
						ex = e;
					}
				}
			}
			throw ex;
		} catch (DatatypeConfigurationException e) {
			return null;
		}
	}

	public static String cleanISO8601(String lexical) {
		// replace dashes in time by colons
		String[] parts = lexical.split("T");
		if (parts.length != 2) {
			return lexical;
		}

		parts[1] = parts[1].substring(0, 2) + ":" + parts[1].substring(3, 5)
				+ ":" + parts[1].substring(6);
		lexical = parts[0] + "T" + parts[1];

		// insert colon in timezone
		String timeZoneVorzeichen = lexical.substring(lexical.length() - 5,
				lexical.length() - 4);
		if (timeZoneVorzeichen.equals("+") || timeZoneVorzeichen.equals("-")) {
			lexical = lexical.substring(0, lexical.length() - 2) + ":"
					+ lexical.substring(lexical.length() - 2);
		}

		return lexical;
	}

}
