package com.bkahlert.devel.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Test;

public class CalendarUtilsTest {

	private Calendar cal1;
	private Calendar cal2;
	private Calendar cal3;

	public CalendarUtilsTest() {
		this.cal1 = Calendar.getInstance();
		this.cal1.set(1975, 12, 15);

		this.cal2 = Calendar.getInstance();
		this.cal2.set(1984, 4, 15);

		this.cal3 = Calendar.getInstance();
		this.cal3.set(1993, 01, 01);
	}

	@Test
	public void testGetEarliestCalendar() {
		assertEquals(cal1,
				CalendarUtils.getEarliestCalendar(Arrays.asList(cal1)));
		assertEquals(cal2,
				CalendarUtils.getEarliestCalendar(Arrays.asList(cal2)));
		assertEquals(cal3,
				CalendarUtils.getEarliestCalendar(Arrays.asList(cal3)));
		assertEquals(cal1, CalendarUtils.getEarliestCalendar(Arrays.asList(
				cal1, cal2, cal3)));
		assertEquals(cal1, CalendarUtils.getEarliestCalendar(Arrays.asList(
				cal2, cal3, cal1)));
		assertEquals(cal1, CalendarUtils.getEarliestCalendar(Arrays.asList(
				cal3, cal1, cal2)));
	}

	@Test
	public void testGetLatestCalendar() {
		assertEquals(cal1, CalendarUtils.getLatestCalendar(Arrays.asList(cal1)));
		assertEquals(cal2, CalendarUtils.getLatestCalendar(Arrays.asList(cal2)));
		assertEquals(cal3, CalendarUtils.getLatestCalendar(Arrays.asList(cal3)));
		assertEquals(cal3, CalendarUtils.getLatestCalendar(Arrays.asList(cal1,
				cal2, cal3)));
		assertEquals(cal3, CalendarUtils.getLatestCalendar(Arrays.asList(cal2,
				cal3, cal1)));
		assertEquals(cal3, CalendarUtils.getLatestCalendar(Arrays.asList(cal3,
				cal1, cal2)));
	}

}
