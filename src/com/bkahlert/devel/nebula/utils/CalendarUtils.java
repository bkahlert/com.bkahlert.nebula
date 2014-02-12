package com.bkahlert.devel.nebula.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class CalendarUtils {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(CalendarUtils.class);

	// Input
	public static final String DATE_PATTERN = "([0-9]+)[^\\d]([0-9]+)[^\\d]([0-9]+)";
	public static final String TIME_PATTERN = DATE_PATTERN + "(\\.([0-9]+))?";
	public static final String TIMEZONE_PATTERN = "(([+-])(([0-9]+)[^\\d]([0-9]+)|([0-9]{1,2})([0-9]{2})))?";
	public static final Pattern FROM_ISO8601 = Pattern.compile(DATE_PATTERN
			+ "T" + TIME_PATTERN + TIMEZONE_PATTERN);
	public static final int FRACTION_LENGTH = 3;

	// Output
	public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String ISO8601_FILESYSTEM = "yyyy-MM-dd'T'HH-mm-ss.SSSZ";

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
		SimpleDateFormat iso8601 = new SimpleDateFormat(ISO8601);
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2) + ":"
				+ missingDots.substring(missingDots.length() - 2);
	}

	public static String toISO8601FileSystemCompatible(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(ISO8601_FILESYSTEM);
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2)
				+ missingDots.substring(missingDots.length() - 2);
	}

	public static Calendar fromISO8601(String lexicalRepresentation)
			throws IllegalArgumentException {
		Matcher matcher = FROM_ISO8601.matcher(lexicalRepresentation);
		if (matcher.find()) {
			String year = matcher.group(1);
			String month = matcher.group(2);
			String date = matcher.group(3);
			String hour = matcher.group(4);
			String minute = matcher.group(5);
			String second = matcher.group(6);
			String fraction = StringUtils.rightPad(
					matcher.group(8) != null ? matcher.group(8) : "",
					FRACTION_LENGTH, "0");

			int year_ = Integer.valueOf(year);
			int month_ = Integer.valueOf(month) - 1;
			int date_ = Integer.valueOf(date);
			int hour_ = Integer.valueOf(hour);
			int minute_ = Integer.valueOf(minute);
			int second_ = Integer.valueOf(second);
			int fraction_ = (int) Math.round(Double.valueOf(fraction.substring(
					0, FRACTION_LENGTH)
					+ "."
					+ fraction.substring(FRACTION_LENGTH)));

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, year_);
			calendar.set(Calendar.MONTH, month_);
			calendar.set(Calendar.DATE, date_);
			calendar.set(Calendar.HOUR_OF_DAY, hour_);
			calendar.set(Calendar.MINUTE, minute_);
			calendar.set(Calendar.SECOND, second_);
			calendar.set(Calendar.MILLISECOND, fraction_);

			String timezoneSign;
			Integer timezoneHour_;
			Integer timezoneMinute_;
			if (matcher.group(9) != null) {
				timezoneSign = matcher.group(10);
				String timezoneHour = matcher.group(12) != null ? matcher
						.group(12) : matcher.group(14);
				String timezoneMinute = matcher.group(13) != null ? matcher
						.group(13) : matcher.group(15);

				timezoneHour_ = Integer.valueOf(timezoneHour) % 24;
				timezoneMinute_ = Integer.valueOf(timezoneMinute) % 60;
			} else {
				int offset = TimeZone.getDefault().getOffset(
						calendar.getTimeInMillis());
				calendar.add(Calendar.MILLISECOND, offset);

				timezoneSign = offset >= 0 ? "+" : "-";
				/*
				 * Double x = offset / 3600000.0; String[] parts =
				 * x.toString().split("\\.");
				 * 
				 * timezoneHour_ = Integer.valueOf(parts[0]); timezoneMinute_ =
				 * (int) (Double.valueOf("." + parts[1]) * 60);
				 */
				timezoneHour_ = timezoneMinute_ = 0;
			}

			TimeZone timeZone = TimeZone.getTimeZone("GMT" + timezoneSign
					+ StringUtils.leftPad(timezoneHour_.toString(), 2, "0")
					+ ":"
					+ StringUtils.leftPad(timezoneMinute_.toString(), 2, "0"));
			calendar.setTimeZone(timeZone);

			return calendar;
		}
		throw new IllegalArgumentException("Could not parse "
				+ lexicalRepresentation);
	}

	public static String toShort(Calendar calendar) {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(calendar.getTime());
	}

	/**
	 * Formats the given {@link Calendar} while considering the time zone
	 * information.
	 * 
	 * @param calendar
	 * @param pattern
	 * @return
	 */
	public static String format(Calendar calendar, String pattern) {
		return format(calendar, new SimpleDateFormat(pattern));
	}

	/**
	 * Formats the given {@link Calendar} while considering the time zone
	 * information.
	 * 
	 * @param calendar
	 * @param dateFormat
	 * @return
	 */
	public static String format(Calendar calendar, DateFormat dateFormat) {
		dateFormat.setTimeZone(calendar.getTimeZone());
		return dateFormat.format(calendar.getTime());
	}

	public static int compareToTimeZoneLess(Calendar calendar1,
			Calendar calendar2) {
		long localTime = CalendarUtils.getLocalTime(calendar1);
		long otherLocalTime = CalendarUtils.getLocalTime(calendar2);
		return new Long(localTime).compareTo(otherLocalTime);
	}

	public static boolean before(Calendar calendar1, Calendar calendar2) {
		return calendar2.compareTo(calendar1) > 0;
	}

	public static boolean after(Calendar calendar1, Calendar calendar2) {
		return calendar2.compareTo(calendar1) < 0;
	}

	/**
	 * Return the milliseconds passed since 1.1.1970 00:00:00.000 [TimeZone]
	 * 
	 * @param calendar
	 * @return
	 */
	public static long getLocalTime(Calendar calendar) {
		return calendar.getTimeInMillis()
				+ calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
	}

	public static Calendar addMilliseconds(Calendar calendar, Long amount) {
		if (amount != null) {
			calendar.add(Calendar.MILLISECOND, (int) amount.floatValue());
		}
		return calendar;
	}

}
