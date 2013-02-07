package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.HashMap;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;

public class Options extends HashMap<String, Object> implements IOptions {

	private static final long serialVersionUID = 3104133404424383886L;

	@Override
	public void setTitle(String title) {
		this.put("title", title);
	}

	@Override
	public void setCenterStart(Calendar calendar) {
		String centerStart = calendar != null ? CalendarUtils
				.toISO8601(calendar) : null;
		this.put("centerStart", centerStart);
	}

	@Override
	public void setTapeImpreciseOpacity(Float opacity) {
		Integer value = null;
		if (opacity != null)
			value = Math.round(opacity / 100f);
		this.put("tape_impreciseOpacity", value);
	}

	@Override
	public void setIconWidth(Integer iconWidth) {
		this.put("icon_width", iconWidth);
	}

	@Override
	public void setBubbleFunction(String functionName, String functionField) {
		this.put("show_bubble", functionName);
		this.put("show_bubble_field", functionField);
	}

	@Override
	public void setHotZones(IHotZone[] hotZones) {
		this.put("hotZones", hotZones);
	}

	@Override
	public void setDecorators(IDecorator[] decorators) {
		this.put("decorators", decorators);
	}

	@Override
	public void setTimeZone(Float offset) {
		this.put("timeZone", offset);
	}

	@Override
	public void setShowInOverviewBands(boolean showInOverviewBands) {
		this.put("showInOverviewBands", showInOverviewBands);
	}

	@Override
	public void setRatio(float ratio) {
		if (ratio < 0 || ratio > 1)
			throw new InvalidParameterException("ratio must be between 0 and 1");
		this.put("width", (int) Math.round(ratio * 100f));
	}
}
