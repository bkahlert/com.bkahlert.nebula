package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.util.Calendar;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.timeline.IDecorator;

public class Decorator implements IDecorator {
	private String startDate;
	private String endDate;
	private String startLabel;
	private String endLabel;

	public Decorator(String startDateDateISO8601, String startLabel,
			String endDateDateISO8601, String endLabel) {
		this.startDate = startDateDateISO8601;
		this.endDate = endDateDateISO8601;
		this.startLabel = startLabel;
		this.endLabel = endLabel;
	}

	public Decorator(Calendar startCalendar, String startLabel,
			Calendar endCalendar, String endLabel) {
		this(startCalendar != null ? CalendarUtils
				.toISO8601(startCalendar) : null, startLabel,
				endCalendar != null ? CalendarUtils
						.toISO8601(endCalendar) : null, endLabel);
	}

	public Decorator(String startDateDateISO8601, String endDateDateISO8601) {
		this(startDateDateISO8601, null, endDateDateISO8601, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.widgets.timeline.impl.IDecorator#getStartDate()
	 */
	@Override
	public String getStartDate() {
		return this.startDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.widgets.timeline.impl.IDecorator#getEndDate()
	 */
	@Override
	public String getEndDate() {
		return this.endDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.widgets.timeline.impl.IDecorator#getStartLabel
	 * ()
	 */
	@Override
	public String getStartLabel() {
		return this.startLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.widgets.timeline.impl.IDecorator#getEndLabel()
	 */
	@Override
	public String getEndLabel() {
		return this.endLabel;
	}
}