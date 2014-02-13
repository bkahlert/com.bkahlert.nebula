package com.bkahlert.devel.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.nebula.datetime.CalendarRange;

public class CalendarUtilsTest {

	private final Calendar cal1;
	private final Calendar cal2;
	private final Calendar cal3;

	public CalendarUtilsTest() {
		this.cal1 = Calendar.getInstance();
		this.cal1.set(1975, 12, 15);

		this.cal2 = Calendar.getInstance();
		this.cal2.set(1984, 4, 15);

		this.cal3 = Calendar.getInstance();
		this.cal3.set(1993, 01, 01);
	}

	@Test
	public void defaultConstructor() {
		Assert.assertTrue(Math.abs(new Date().getTime()
				- Calendar.getInstance().getTimeInMillis()) < 10);
	}

	@Test
	public void testToFromISO8601() throws Exception {
		// complete
		assertEquals("1984-05-15T14:30:04.123+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.123+01:30")));
		assertEquals("1984-05-15T14:30:04.123-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.123-02:45")));

		// rounding
		assertEquals("1999-12-31T23:59:59.999+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1999-12-31T23:59:59.9994+01:30")));
		assertEquals("2000-01-01T00:00:00.000-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1999-12-31T23:59:59.9995-02:45")));

		// too many characters in milliseconds
		assertEquals("1984-05-15T14:30:04.123+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.1234+01:30")));
		assertEquals("1984-05-15T14:30:04.124-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.1235-02:45")));

		// missing colon in timezone
		assertEquals("1984-05-15T14:30:04.123+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.123+01:30")));
		assertEquals("1984-05-15T14:30:04.123-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14:30:04.123-0245")));

		// dashes instead of colons in time
		assertEquals("1984-05-15T14:30:04.123+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14-30-04.123+01:30")));
		assertEquals("1984-05-15T14:30:04.123-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14-30-04.123-0245")));

		// missing timezone
		assertEquals("1984-05-15T14:30:04.123+00:00",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14-30-04.123")));

		// missing milliseconds
		assertEquals("1984-05-15T14:30:04.120+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14-30-04.12+01:30")));
		assertEquals("1984-05-15T14:30:04.000-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-15T14-30-04-0245")));

		// missing digits
		assertEquals("1984-05-15T14:30:04.120+01:30",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-5-15T14-30-4.12+01:30")));
		assertEquals("1984-05-05T14:30:04.000-02:45",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("1984-05-5T14-30-04-245")));
		assertEquals("0984-05-05T14:30:04.000-02:05",
				CalendarUtils.toISO8601(CalendarUtils
						.fromISO8601("984-05-5T14-30-04-2:5")));

		try {
			CalendarUtils.fromISO8601("1984-05-15-14:30:04.123-02:45");
			assertTrue(false);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testFormat() throws ParseException {
		String pattern = "yyyy-MM-dd HH:mm:ss Z";

		Assert.assertEquals("2011-11-18 15:38:28 +0900", CalendarUtils.format(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28.0+09:00"),
				pattern));
		Assert.assertEquals("1984-05-15 14:30:00 +0900",
				CalendarUtils.format(
						CalendarUtils.fromISO8601("1984-05-15T14:30:00+09:00"),
						pattern));
	}

	@Test
	public void testGetTime() throws ParseException {
		Assert.assertEquals(1321598308000l,
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")
						.getTimeInMillis());
		Assert.assertEquals(1321643308000l,
				CalendarUtils.fromISO8601("2011-11-18T15:38:28-03:30.003123")
						.getTimeInMillis());
	}

	@Test
	public void testGetTimeZone() throws ParseException {
		Assert.assertEquals(TimeZone.getTimeZone("GMT+09:00"), CalendarUtils
				.fromISO8601("2011-11-18T15:38:28+09:00").getTimeZone());
		Assert.assertEquals(TimeZone.getTimeZone("GMT-05:00"), CalendarUtils
				.fromISO8601("2011-11-18T15:38:28-05:00").getTimeZone());
		Assert.assertEquals(TimeZone.getTimeZone("GMT+00:00"), CalendarUtils
				.fromISO8601("2011-11-18T15:38:28").getTimeZone());
	}

	@Test
	public void testCompareTo() throws ParseException {
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-18T15:38:28+09:00").compareTo(
						CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) == 0);
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-18T15:38:28+09:00").compareTo(
						CalendarUtils.fromISO8601("2011-11-18T15:38:28+08:00")) < 0);
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-17T15:38:28+09:00").compareTo(
						CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) < 0);
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-19T15:38:28+09:00").compareTo(
						CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) > 0);
	}

	@Test
	public void testCompareToTimeZoneLess() throws ParseException {
		Assert.assertTrue(CalendarUtils.compareToTimeZoneLess(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) == 0);
		Assert.assertTrue(CalendarUtils.compareToTimeZoneLess(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+08:00")) == 0);
		Assert.assertTrue(CalendarUtils.compareToTimeZoneLess(
				CalendarUtils.fromISO8601("2011-11-17T15:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) < 0);
		Assert.assertTrue(CalendarUtils.compareToTimeZoneLess(
				CalendarUtils.fromISO8601("2011-11-19T15:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")) > 0);
	}

	@Test
	public void testEquals() throws ParseException {
		Assert.assertEquals(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28.000+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00"));
		Assert.assertEquals(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28.100+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T15:38:28.1+09:00"));
		Assert.assertEquals(
				CalendarUtils.fromISO8601("2011-11-19T15:38:28.599+09:00"),
				CalendarUtils.fromISO8601("2011-11-19T15:38:28.5991+09:00"));
	}

	@Test
	public void testAfter() throws ParseException {
		Assert.assertFalse(CalendarUtils.fromISO8601(
				"2011-11-18T15:38:28+09:00").after(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")));
		Assert.assertFalse(CalendarUtils.fromISO8601(
				"2011-11-18T15:38:28+09:00").after(
				CalendarUtils.fromISO8601("2011-11-18T15:38:28+08:00")));
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-19T15:38:28+09:00").after(
						CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00")));
	}

	@Test
	public void testAddMilliseconds() throws ParseException {
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-18T15:38:33+09:00").compareTo(
						CalendarUtils.addMilliseconds(CalendarUtils
								.fromISO8601("2011-11-18T15:38:28+09:00"),
								5000l)) == 0);
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-18T15:38:20+08:00").compareTo(
						CalendarUtils.addMilliseconds(CalendarUtils
								.fromISO8601("2011-11-18T15:38:28+08:00"),
								-8000l)) == 0);
		Assert.assertTrue(CalendarUtils
				.fromISO8601("2011-11-18T15:38:20+08:00").compareTo(
						CalendarUtils.addMilliseconds(CalendarUtils
								.fromISO8601("2011-11-18T15:38:28+08:00"),
								-8000l)) == 0);
	}

	@Test
	public void testClone() throws ParseException {
		Calendar original = CalendarUtils
				.fromISO8601("2011-11-18T15:38:28+09:00");
		Calendar clone = (Calendar) original.clone();
		CalendarUtils.addMilliseconds(clone, 1000l);
		Assert.assertFalse(original.getTimeInMillis() == clone
				.getTimeInMillis());
		Assert.assertTrue(original.getTimeInMillis() - clone.getTimeInMillis() == -1000);
	}

	@Test
	public void testGetEarliestCalendar() {
		assertEquals(this.cal1,
				CalendarUtils.getEarliestCalendar(Arrays.asList(this.cal1)));
		assertEquals(this.cal2,
				CalendarUtils.getEarliestCalendar(Arrays.asList(this.cal2)));
		assertEquals(this.cal3,
				CalendarUtils.getEarliestCalendar(Arrays.asList(this.cal3)));
		assertEquals(this.cal1, CalendarUtils.getEarliestCalendar(Arrays
				.asList(this.cal1, this.cal2, this.cal3)));
		assertEquals(this.cal1, CalendarUtils.getEarliestCalendar(Arrays
				.asList(this.cal2, this.cal3, this.cal1)));
		assertEquals(this.cal1, CalendarUtils.getEarliestCalendar(Arrays
				.asList(this.cal3, this.cal1, this.cal2)));
	}

	@Test
	public void testGetLatestCalendar() {
		assertEquals(this.cal1,
				CalendarUtils.getLatestCalendar(Arrays.asList(this.cal1)));
		assertEquals(this.cal2,
				CalendarUtils.getLatestCalendar(Arrays.asList(this.cal2)));
		assertEquals(this.cal3,
				CalendarUtils.getLatestCalendar(Arrays.asList(this.cal3)));
		assertEquals(this.cal3, CalendarUtils.getLatestCalendar(Arrays.asList(
				this.cal1, this.cal2, this.cal3)));
		assertEquals(this.cal3, CalendarUtils.getLatestCalendar(Arrays.asList(
				this.cal2, this.cal3, this.cal1)));
		assertEquals(this.cal3, CalendarUtils.getLatestCalendar(Arrays.asList(
				this.cal3, this.cal1, this.cal2)));
	}

	@Test
	public void testShortener() {
		assertEquals(true, CalendarRange.DURATION_SHORTENER.matcher("00 h")
				.matches());
		assertEquals(true, CalendarRange.DURATION_SHORTENER.matcher("00h")
				.matches());
		assertEquals(true, CalendarRange.DURATION_SHORTENER.matcher("0s")
				.matches());
		assertEquals(true, CalendarRange.DURATION_SHORTENER.matcher("0000")
				.matches());
		assertEquals(false, CalendarRange.DURATION_SHORTENER.matcher("01 h")
				.matches());
		assertEquals(false, CalendarRange.DURATION_SHORTENER.matcher("1")
				.matches());
		assertEquals(false, CalendarRange.DURATION_SHORTENER.matcher("abc")
				.matches());
	}

	@Test
	public void testFormatDuration() {
		final String format = "MM'm' ddddd'd' HH'h'";
		assertEquals("00m 03042d 23h",
				new CalendarRange(this.cal1, this.cal2).formatDuration(format));
		assertEquals("00m 06227d 00h",
				new CalendarRange(this.cal1, this.cal3).formatDuration(format));
		assertEquals("00m 03184d 01h",
				new CalendarRange(this.cal2, this.cal3).formatDuration(format));
	}

	@Test
	public void testFormatDurationNicely() {
		final String format = "MM'm' ddddd'd' HH'h'";
		assertEquals("3042d 23h",
				new CalendarRange(this.cal1, this.cal2)
						.formatDurationNicely(format));
		assertEquals("6227d 00h",
				new CalendarRange(this.cal1, this.cal3)
						.formatDurationNicely(format));
		assertEquals("3184d 01h",
				new CalendarRange(this.cal2, this.cal3)
						.formatDurationNicely(format));
	}

}
