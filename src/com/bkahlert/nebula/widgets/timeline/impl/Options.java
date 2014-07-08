package com.bkahlert.nebula.widgets.timeline.impl;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.HashMap;

import com.bkahlert.nebula.utils.CalendarUtils;
import com.bkahlert.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.nebula.widgets.timeline.model.IHotZone;
import com.bkahlert.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.nebula.widgets.timeline.model.IZoomStep;

public class Options extends HashMap<String, Object> implements IOptions {

	private static final long serialVersionUID = 3104133404424383886L;

	@Override
	public void setTitle(String title) {
		this.put("title", title);
	}

	@Override
	public String getTitle() {
		return (String) this.get("title");
	}

	@Override
	public void setCenterStart(Calendar calendar) {
		String centerStart = calendar != null ? CalendarUtils
				.toISO8601(calendar) : null;
		this.put("centerStart", centerStart);
	}

	@Override
	public Calendar getCenterStart() {
		String centerStart = (String) this.get("centerStart");
		if (centerStart != null)
			return CalendarUtils.fromISO8601(centerStart);
		else
			return null;
	}

	@Override
	public void setTapeImpreciseOpacity(Float opacity) {
		Integer value = null;
		if (opacity != null)
			value = Math.round(opacity / 100f);
		this.put("tape_impreciseOpacity", value);
	}

	@Override
	public Float getTapeImpreciseOpacity() {
		return (Float) this.get("tape_impreciseOpacity");
	}

	@Override
	public void setIconWidth(Integer iconWidth) {
		this.put("icon_width", iconWidth);
	}

	@Override
	public Integer getIconWidth() {
		return (Integer) this.get("icon_width");
	}

	@Override
	public void setBubbleFunction(String functionName, String functionField) {
		this.put("show_bubble", functionName);
		this.put("show_bubble_field", functionField);
	}

	@Override
	public String[] getBubbleFunction() {
		return new String[] { (String) this.get("show_bubble"),
				(String) this.get("show_bubble_field") };
	}

	@Override
	public void setHotZones(IHotZone[] hotZones) {
		this.put("hotZones", hotZones);
	}

	@Override
	public IHotZone[] getHotZones() {
		return (IHotZone[]) this.get("hotZones");
	}

	@Override
	public void setPermanentDecorators(IDecorator[] decorators) {
		this.put("permanentDecorators", decorators);
	}

	@Override
	public IDecorator[] getPermanentDecorators() {
		return (IDecorator[]) this.get("permanentDecorators");
	}

	@Override
	public void setDecorators(IDecorator[] decorators) {
		this.put("decorators", decorators);
	}

	@Override
	public IDecorator[] getDecorators() {
		return (IDecorator[]) this.get("decorators");
	}

	@Override
	public void setTimeZone(Float offset) {
		this.put("timeZone", offset);
	}

	@Override
	public Float getTimeZone() {
		return (Float) this.get("timeZone");
	}

	@Override
	public void setShowInOverviewBands(Boolean showInOverviewBands) {
		this.put("showInOverviewBands", showInOverviewBands);
	}

	@Override
	public Boolean getShowInOverviewBands() {
		return (Boolean) this.get("showInOverviewBands");
	}

	@Override
	public void setRatio(Float ratio) {
		if (ratio == null) {
			this.put("width", null);
			return;
		}

		if (ratio < 0 || ratio > 1)
			throw new InvalidParameterException("ratio must be between 0 and 1");
		this.put("width", (int) Math.round(ratio * 100f));
	}

	@Override
	public Float getRatio() {
		return (Float) this.get("width");
	}

	@Override
	public void setZoomSteps(IZoomStep[] zoomSteps) {
		this.put("zoomSteps", zoomSteps);
	}

	@Override
	public IZoomStep[] getZoomSteps() {
		return (IZoomStep[]) this.get("zoomSteps");
	}

	@Override
	public void setZoomIndex(Integer zoomIndex) {
		this.put("zoomIndex", zoomIndex);
	}

	@Override
	public Integer getZoomIndex() {
		return (Integer) this.get("zoomIndex");
	}
}
