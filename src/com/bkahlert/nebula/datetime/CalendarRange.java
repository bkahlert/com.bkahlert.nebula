package com.bkahlert.nebula.datetime;

import java.security.InvalidParameterException;
import java.util.Calendar;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

/**
 * This class describes a range defined by two {@link TimeZoneDate}s. To define
 * a unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class CalendarRange implements Comparable<CalendarRange> {

	public static CalendarRange calculateOuterDateRange(
			CalendarRange... dateRanges) {
		Calendar earliestDate = null;
		Calendar latestDate = null;

		for (CalendarRange calendarRange : dateRanges) {
			if (calendarRange == null) {
				continue;
			}
			if (calendarRange.getStartDate() != null
					&& (earliestDate == null || earliestDate
							.compareTo(calendarRange.getStartDate()) > 0)) {
				earliestDate = calendarRange.getStartDate();
			}
			if (calendarRange.getEndDate() != null
					&& (latestDate == null || latestDate.compareTo(calendarRange
							.getEndDate()) < 0)) {
				latestDate = calendarRange.getEndDate();
			}
		}

		return new CalendarRange(earliestDate, latestDate);
	}

	private final Calendar startDate;
	private final Calendar endDate;

	public CalendarRange(Calendar startDate, Calendar endDate) {
		super();
		if (startDate != null && endDate != null
				&& startDate.compareTo(endDate) > 0) {
			throw new InvalidParameterException(
					"start date must be before or on the end date");
		}
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Calendar getStartDate() {
		return this.startDate;
	}

	public Calendar getEndDate() {
		return this.endDate;
	}

	public Long getDifference() {
		if (this.startDate == null) {
			return null;
		}
		if (this.endDate == null) {
			return null;
		}
		return this.endDate.getTimeInMillis()
				- this.startDate.getTimeInMillis();
	}

	public boolean isInRange(long time) {
		return (this.startDate == null || this.startDate.getTimeInMillis() <= time)
				&& (this.endDate == null || time <= this.endDate
						.getTimeInMillis());
	}

	public boolean isInRange(Calendar date) {
		if (date == null) {
			return false;
		}
		return this.isInRange(date.getTimeInMillis());
	}

	public boolean isBeforeRange(long time) {
		if (this.startDate == null) {
			return false;
		} else {
			return time < this.startDate.getTimeInMillis();
		}
	}

	public boolean isBeforeRange(Calendar date) {
		if (date == null) {
			return false;
		}
		return this.isBeforeRange(date.getTimeInMillis());
	}

	public boolean isAfterRange(long time) {
		if (this.endDate == null) {
			return false;
		} else {
			return time > this.endDate.getTimeInMillis();
		}
	}

	public boolean isAfterRange(Calendar date) {
		if (date == null) {
			return false;
		}
		return this.isAfterRange(date.getTimeInMillis());
	}

	/**
	 * Returns true if the given {@link CalendarRange} intersects the
	 * current {@link CalendarRange}.
	 * 
	 * @param calendarRange
	 * @return
	 */
	public boolean isIntersected(CalendarRange calendarRange) {
		if (calendarRange == null) {
			return true;
		}

		boolean startAndEndBeforeRange = this.isBeforeRange(calendarRange
				.getStartDate()) && this.isBeforeRange(calendarRange.getEndDate());
		boolean startAndEndAfterRange = this.isAfterRange(calendarRange
				.getStartDate()) && this.isAfterRange(calendarRange.getEndDate());
		return !(startAndEndBeforeRange || startAndEndAfterRange);
	}

	/**
	 * Returns true if the given {@link CalendarRange} intersects the
	 * current {@link CalendarRange}.
	 * <p>
	 * In contrast to {@link #isIntersected(CalendarRange)} this method does
	 * not count exact matches as intersected. This means the the case in which
	 * one {@link CalendarRange} ends at the very moment the second one
	 * starts is not considered intersected.
	 * 
	 * @param calendarRange
	 * @return
	 */
	public boolean isIntersected2(CalendarRange calendarRange) {
		if (!this.isIntersected(calendarRange)) {
			return false;
		}
		boolean areNeighbors1 = this.endDate != null
				&& calendarRange.getStartDate() != null
				&& this.endDate.equals(calendarRange.getStartDate());
		boolean areNeighbors2 = this.startDate != null
				&& calendarRange.getEndDate() != null
				&& this.startDate.equals(calendarRange.endDate);
		return !areNeighbors1 && !areNeighbors2;
	}

	@Override
	public int compareTo(CalendarRange o) {
		Calendar t1 = this.getStartDate();
		if (t1 == null) {
			t1 = this.getEndDate();
		}

		Calendar t2 = o.getStartDate();
		if (t2 == null) {
			t2 = o.getEndDate();
		}

		if (t1 == null && t2 == null) {
			return 0;
		}
		if (t1 != null && t2 == null) {
			return +1;
		}
		if (t1 == null && t2 != null) {
			return -1;
		}
		return t1.compareTo(t2);
	}

	@Override
	public String toString() {
		return ((this.startDate != null) ? CalendarUtils
				.toISO8601(this.startDate) : "-inf")
				+ " - "
				+ ((this.endDate != null) ? CalendarUtils
						.toISO8601(this.endDate) : "+inf");
	}

}
