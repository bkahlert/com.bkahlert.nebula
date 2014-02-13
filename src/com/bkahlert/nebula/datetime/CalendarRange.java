package com.bkahlert.nebula.datetime;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import com.bkahlert.devel.nebula.utils.CalendarUtils;

/**
 * This class describes a range defined by two {@link TimeZoneDate}s. To define
 * a unbounded range, simply pass <code>null</code> as one argument.
 * 
 * @author bkahlert
 */
public class CalendarRange implements Comparable<CalendarRange> {

	public static Pattern DURATION_SHORTENER = Pattern
			.compile("0+\\s*[A-Za-z]*");

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
					&& (latestDate == null || latestDate
							.compareTo(calendarRange.getEndDate()) < 0)) {
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
	 * Returns true if the given {@link CalendarRange} intersects the current
	 * {@link CalendarRange}.
	 * 
	 * @param calendarRange
	 * @return
	 */
	public boolean isIntersected(CalendarRange calendarRange) {
		if (calendarRange == null) {
			return true;
		}

		boolean startAndEndBeforeRange = this.isBeforeRange(calendarRange
				.getStartDate())
				&& this.isBeforeRange(calendarRange.getEndDate());
		boolean startAndEndAfterRange = this.isAfterRange(calendarRange
				.getStartDate())
				&& this.isAfterRange(calendarRange.getEndDate());
		return !(startAndEndBeforeRange || startAndEndAfterRange);
	}

	/**
	 * Returns true if the given {@link CalendarRange} intersects the current
	 * {@link CalendarRange}.
	 * <p>
	 * In contrast to {@link #isIntersected(CalendarRange)} this method does not
	 * count exact matches as intersected. This means the the case in which one
	 * {@link CalendarRange} ends at the very moment the second one starts is
	 * not considered intersected.
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

	/**
	 * Formats this {@link CalendarRange}'s time difference / duration according
	 * to the given duration format.
	 * 
	 * @param durationFormat
	 * @return
	 */
	public String formatDuration(String durationFormat) {
		Long milliSecondsPassed = this.getDifference();
		if (milliSecondsPassed == null) {
			return "?";
		}
		return DurationFormatUtils.formatDuration(milliSecondsPassed,
				durationFormat, true);
	}

	/**
	 * Formats this {@link CalendarRange}'s time difference / duration according
	 * to the given duration format nicely.
	 * <p>
	 * That means leading zeros are removed. E.g. the normally formated duration
	 * <code>00d 05h 30m</code> would result in <code>5h 30m</code>.
	 * 
	 * @param durationFormat
	 * @return
	 */
	public String formatDurationNicely(String durationFormat) {
		String duration = this.formatDuration(durationFormat);
		List<String> parts = new LinkedList<String>(Arrays.asList(duration
				.split(" ")));
		for (Iterator<String> it = parts.iterator(); it.hasNext();) {
			String part = it.next();
			// remove parts only consisting of zeros
			if (it.hasNext() && DURATION_SHORTENER.matcher(part).matches()) {
				it.remove();
			} else {
				break;
			}
		}
		// invariant: no leading parts that are only zero
		if (parts.size() > 0) {
			// remove leading zeros
			parts.set(0, parts.get(0).replaceAll("^0+", ""));

			// make sure at least one digit stays
			if (!Character.isDigit(parts.get(0).charAt(0))) {
				parts.set(0, "0" + parts.get(0));
			}
		}
		return StringUtils.join(parts, " ");
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
