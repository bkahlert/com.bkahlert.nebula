package com.bkahlert.devel.nebula.widgets.timeline.impl;

import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;

public class HotZone implements IHotZone {
	private String start;
	private String end;

	public HotZone(String startDateISO8601, String endDateISO8601) {
		this.start = startDateISO8601;
		this.end = endDateISO8601;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.widgets.timeline.impl.IHotZone#getStart()
	 */
	@Override
	public String getStart() {
		return this.start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.widgets.timeline.impl.IHotZone#getEnd()
	 */
	@Override
	public String getEnd() {
		return this.end;
	}
}