package com.bkahlert.nebula.datetime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

public class CalendarRangeTest {

	// -2 years
	private final Calendar muchBeforeRangeDate = CalendarUtils
			.fromISO8601("1982-05-15T14:30:00+01:00");

	// -1 year, -1 month
	private final Calendar beforeRangeDate = CalendarUtils
			.fromISO8601("1983-04-15T14:30:00+01:00");

	// -1 year
	private final Calendar rangeStart = CalendarUtils
			.fromISO8601("1983-05-15T14:30:00+01:00");

	// fixed
	private final Calendar inRangeDate = CalendarUtils
			.fromISO8601("1984-05-15T14:30:00+01:00");

	// +1 year
	private final Calendar rangeEnd = CalendarUtils
			.fromISO8601("1985-05-15T14:30:00+01:00");

	// +1 year, +1 month
	private final Calendar afterRangeDate = CalendarUtils
			.fromISO8601("1985-06-15T14:30:00+01:00");

	// -2 years
	private final Calendar muchAfterRangeDate = CalendarUtils
			.fromISO8601("1986-05-15T14:30:00+01:00");

	@Test
	public void testValidRange() {
		CalendarRange calendarRange = new CalendarRange(this.rangeStart, this.rangeEnd);
		Assert.assertEquals(this.rangeStart, calendarRange.getStartDate());
		Assert.assertEquals(this.rangeEnd, calendarRange.getEndDate());

		calendarRange = new CalendarRange(this.rangeStart, this.rangeStart);
		Assert.assertEquals(this.rangeStart, calendarRange.getStartDate());
		Assert.assertEquals(this.rangeStart, calendarRange.getEndDate());

		calendarRange = new CalendarRange(this.rangeStart, null);
		Assert.assertEquals(this.rangeStart, calendarRange.getStartDate());
		Assert.assertNull(calendarRange.getEndDate());

		calendarRange = new CalendarRange(null, this.rangeEnd);
		Assert.assertNull(calendarRange.getStartDate());
		Assert.assertEquals(this.rangeEnd, calendarRange.getEndDate());

		calendarRange = new CalendarRange(null, null);
		Assert.assertNull(calendarRange.getStartDate());
		Assert.assertNull(calendarRange.getEndDate());
	}

	@Test(expected = InvalidParameterException.class)
	public void testInvalidRange() {
		new CalendarRange(CalendarUtils.fromISO8601("2011-11-18T15:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T14:38:28+09:00"));
	}

	@Test(expected = InvalidParameterException.class)
	public void testInvalidRange2() {
		new CalendarRange(CalendarUtils.fromISO8601("2011-11-18T14:38:28+09:00"),
				CalendarUtils.fromISO8601("2011-11-18T14:38:28+10:00"));
	}

	@Test
	public void boundedRangeDateTest() {
		CalendarRange boundedRangeDate = new CalendarRange(this.rangeStart,
				this.rangeEnd);

		Assert.assertTrue(boundedRangeDate.isBeforeRange(this.beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(this.beforeRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(this.beforeRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(this.inRangeDate));
		Assert.assertTrue(boundedRangeDate.isInRange(this.inRangeDate));
		Assert.assertFalse(boundedRangeDate.isAfterRange(this.inRangeDate));

		Assert.assertFalse(boundedRangeDate.isBeforeRange(this.afterRangeDate));
		Assert.assertFalse(boundedRangeDate.isInRange(this.afterRangeDate));
		Assert.assertTrue(boundedRangeDate.isAfterRange(this.afterRangeDate));

		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchBeforeRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeStart)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.inRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.rangeEnd)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.afterRangeDate)));
		Assert.assertTrue(boundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.afterRangeDate)));
		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.muchAfterRangeDate)));

		Assert.assertFalse(boundedRangeDate.isIntersected(new CalendarRange(
				this.muchAfterRangeDate, this.muchAfterRangeDate)));
	}

	@Test
	public void leftUnboundedRangeDateTest() {
		CalendarRange leftUnboundedRangeDate = new CalendarRange(null, this.rangeEnd);

		Assert.assertFalse(leftUnboundedRangeDate
				.isBeforeRange(this.beforeRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate
				.isInRange(this.beforeRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate
				.isAfterRange(this.beforeRangeDate));

		Assert.assertFalse(leftUnboundedRangeDate
				.isBeforeRange(this.inRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate.isInRange(this.inRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate
				.isAfterRange(this.inRangeDate));

		Assert.assertFalse(leftUnboundedRangeDate
				.isBeforeRange(this.afterRangeDate));
		Assert.assertFalse(leftUnboundedRangeDate
				.isInRange(this.afterRangeDate));
		Assert.assertTrue(leftUnboundedRangeDate
				.isAfterRange(this.afterRangeDate));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchBeforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeStart)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.inRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.rangeEnd)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.afterRangeDate)));
		Assert.assertTrue(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.afterRangeDate)));
		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.muchAfterRangeDate)));

		Assert.assertFalse(leftUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchAfterRangeDate, this.muchAfterRangeDate)));
	}

	@Test
	public void rightUnboundedRangeDateTest() {
		CalendarRange rightUnboundedRangeDate = new CalendarRange(this.rangeStart, null);

		Assert.assertTrue(rightUnboundedRangeDate
				.isBeforeRange(this.beforeRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate
				.isInRange(this.beforeRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate
				.isAfterRange(this.beforeRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate
				.isBeforeRange(this.inRangeDate));
		Assert.assertTrue(rightUnboundedRangeDate.isInRange(this.inRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate
				.isAfterRange(this.inRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate
				.isBeforeRange(this.afterRangeDate));
		Assert.assertTrue(rightUnboundedRangeDate
				.isInRange(this.afterRangeDate));
		Assert.assertFalse(rightUnboundedRangeDate
				.isAfterRange(this.afterRangeDate));

		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchBeforeRangeDate)));
		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertFalse(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeStart)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.inRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.rangeEnd)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.afterRangeDate)));
		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(rightUnboundedRangeDate.isIntersected(new CalendarRange(
				this.muchAfterRangeDate, this.muchAfterRangeDate)));
	}

	@Test
	public void unboundedRangeDateTest() {
		CalendarRange unboundedRangeDate = new CalendarRange(null, null);

		Assert.assertFalse(unboundedRangeDate
				.isBeforeRange(this.beforeRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(this.beforeRangeDate));
		Assert.assertFalse(unboundedRangeDate
				.isAfterRange(this.beforeRangeDate));

		Assert.assertFalse(unboundedRangeDate.isBeforeRange(this.inRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(this.inRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(this.inRangeDate));

		Assert.assertFalse(unboundedRangeDate
				.isBeforeRange(this.afterRangeDate));
		Assert.assertTrue(unboundedRangeDate.isInRange(this.afterRangeDate));
		Assert.assertFalse(unboundedRangeDate.isAfterRange(this.afterRangeDate));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchBeforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.beforeRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeStart)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeStart, this.muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.inRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.inRangeDate, this.muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.rangeEnd)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.rangeEnd, this.muchAfterRangeDate)));

		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.afterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.afterRangeDate, this.muchAfterRangeDate)));
		Assert.assertTrue(unboundedRangeDate.isIntersected(new CalendarRange(
				this.muchAfterRangeDate, this.muchAfterRangeDate)));
	}

	@Test
	public void testCalculateOuterDateRange() {
		CalendarRange outerRange;

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange[0]);
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());

		// one moment
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchBeforeRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getEndDate());

		// one range
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// two ranges
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate),
				new CalendarRange(this.beforeRangeDate, this.afterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate), new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.afterRangeDate), new CalendarRange(
				this.beforeRangeDate, this.muchAfterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate), new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// null
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate), null,
				new CalendarRange(this.beforeRangeDate, this.afterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// left partially unbounded
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(null,
				this.muchAfterRangeDate), null, new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate));
		Assert.assertEquals(this.beforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate), null,
				new CalendarRange(null, this.afterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// left unbounded
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(null,
				this.muchAfterRangeDate), new CalendarRange(null,
				this.afterRangeDate));
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// right partially unbounded
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, null), new CalendarRange(
				this.beforeRangeDate, this.afterRangeDate));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.afterRangeDate, outerRange.getEndDate());

		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, this.muchAfterRangeDate),
				new CalendarRange(this.beforeRangeDate, null));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertEquals(this.muchAfterRangeDate, outerRange.getEndDate());

		// right unbounded
		outerRange = CalendarRange.calculateOuterDateRange(new CalendarRange(
				this.muchBeforeRangeDate, null), new CalendarRange(
				this.beforeRangeDate, null));
		Assert.assertEquals(this.muchBeforeRangeDate, outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());

		// both unbounded
		outerRange = CalendarRange.calculateOuterDateRange(
				new CalendarRange(null, null), new CalendarRange(null, null));
		Assert.assertNull(outerRange.getStartDate());
		Assert.assertNull(outerRange.getEndDate());
	}

	@Test
	public void testIsIntersected() {
		CalendarRange r1 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T14:32:03+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:23+02:00"));
		CalendarRange r2 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T14:32:03+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:24+02:00"));
		CalendarRange r3 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T15:27:23+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:37+02:00"));
		CalendarRange r4 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-14T15:27:23+02:00"),
				CalendarUtils.fromISO8601("2011-09-14T15:27:37+02:00"));

		assertTrue(r1.isIntersected(r2));
		assertTrue(r2.isIntersected(r1));

		assertTrue(r1.isIntersected(r3));
		assertTrue(r3.isIntersected(r1));

		assertFalse(r1.isIntersected(r4));
		assertFalse(r4.isIntersected(r1));

		assertTrue(r2.isIntersected(r3));
		assertTrue(r3.isIntersected(r2));

		assertFalse(r2.isIntersected(r4));
		assertFalse(r4.isIntersected(r2));

		assertFalse(r3.isIntersected(r4));
		assertFalse(r4.isIntersected(r3));
	}

	@Test
	public void testIsIntersected2() {
		CalendarRange r1 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T14:32:03+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:23+02:00"));
		CalendarRange r2 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T14:32:03+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:24+02:00"));
		CalendarRange r3 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-13T15:27:23+02:00"),
				CalendarUtils.fromISO8601("2011-09-13T15:27:37+02:00"));
		CalendarRange r4 = new CalendarRange(
				CalendarUtils.fromISO8601("2011-09-14T15:27:23+02:00"),
				CalendarUtils.fromISO8601("2011-09-14T15:27:37+02:00"));

		assertTrue(r1.isIntersected2(r2));
		assertTrue(r2.isIntersected2(r1));

		assertFalse(r1.isIntersected2(r3));
		assertFalse(r3.isIntersected2(r1));

		assertFalse(r1.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r1));

		assertTrue(r2.isIntersected2(r3));
		assertTrue(r3.isIntersected2(r2));

		assertFalse(r2.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r2));

		assertFalse(r3.isIntersected2(r4));
		assertFalse(r4.isIntersected2(r3));
	}

}
