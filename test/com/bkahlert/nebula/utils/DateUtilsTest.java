package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;

import org.junit.Test;

import com.bkahlert.nebula.utils.DateUtils;

public class DateUtilsTest {

	@Test
	public void testGetMinutesOfTimeZone() {
		assertEquals(120,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23+02:00"));
		assertEquals(130,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23+02:10"));
		assertEquals(0,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23+00:00"));
		assertEquals(0,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23-00:00"));
		assertEquals(-300,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23-05:00"));
		assertEquals(-315,
				DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23-05:15"));
	}

	@Test(expected = InvalidParameterException.class)
	public void testGetMinutesOfTimeZoneIncomplete() {
		DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23+02");
	}

	@Test(expected = InvalidParameterException.class)
	public void testGetMinutesOfTimeZoneInvalidSign() {
		DateUtils.getMinutesOfTimeZone("2010-05-10T13:15:23X02:00");
	}

	@Test
	public void testGetTimeZoneStringFromMinutes() {
		assertEquals("+02:00", DateUtils.getTimeZoneStringFromMinutes(120));
		assertEquals("+02:10", DateUtils.getTimeZoneStringFromMinutes(130));
		assertEquals("+00:00", DateUtils.getTimeZoneStringFromMinutes(0));
		assertEquals("-05:00", DateUtils.getTimeZoneStringFromMinutes(-300));
		assertEquals("-05:15", DateUtils.getTimeZoneStringFromMinutes(-315));
	}

	@Test(expected = InvalidParameterException.class)
	public void testGetMinutesOfTimeZoneExceedingPos() {
		DateUtils.getTimeZoneStringFromMinutes(100 * 60);
	}

	@Test(expected = InvalidParameterException.class)
	public void testGetMinutesOfTimeZoneExceedingNeg() {
		DateUtils.getTimeZoneStringFromMinutes(-100 * 60);
	}

	@Test
	public void testAddMinutesToTimeZone() {
		assertEquals("2010-05-10T13:15:23+02:15",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23+02:00", 15));
		assertEquals("2010-05-10T13:15:23+01:45",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23+02:00", -15));
		assertEquals("2010-05-10T13:15:23+01:55",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23+02:10", -15));
		assertEquals("2010-05-10T13:15:23+00:00",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23+02:10",
						-130));
		assertEquals("2010-05-10T13:15:23-00:05",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23+02:10",
						-135));
		assertEquals("2010-05-10T13:15:23-04:30",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23-05:00", 30));
		assertEquals("2010-05-10T13:15:23-05:30",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23-05:00", -30));
		assertEquals("2010-05-10T13:15:23-04:45",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23-05:15", 30));
		assertEquals("2010-05-10T13:15:23-06:15",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23-05:45", -30));
		assertEquals("2010-05-10T13:15:23+05:30",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23+00:00", 330));
		assertEquals("2010-05-10T13:15:23+06:00",
				DateUtils
						.addMinutesToTimeZone("2010-05-10T13:15:23+00:00", 360));
		assertEquals("2010-05-10T13:15:23-05:30",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23+00:00",
						-330));
		assertEquals("2010-05-10T13:15:23-06:00",
				DateUtils.addMinutesToTimeZone("2010-05-10T13:15:23+00:00",
						-360));
	}
}
