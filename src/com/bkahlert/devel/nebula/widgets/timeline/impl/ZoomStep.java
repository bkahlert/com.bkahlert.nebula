package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.eclipse.core.runtime.Assert;

import com.bkahlert.devel.nebula.widgets.timeline.model.IZoomStep;
import com.bkahlert.devel.nebula.widgets.timeline.model.Unit;

public class ZoomStep implements IZoomStep {

	private float pixelsPerInterval;
	private Unit unit;
	private int showLabelEveryUnits;

	public ZoomStep(float pixelsPerInterval, Unit unit, int showLabelEveryUnits) {
		super();
		Assert.isNotNull(unit);
		this.pixelsPerInterval = pixelsPerInterval;
		this.unit = unit;
		this.showLabelEveryUnits = showLabelEveryUnits;
	}

	@Override
	public float getPixelsPerInterval() {
		return this.pixelsPerInterval;
	}

	@Override
	public Unit getUnit() {
		return this.unit;
	}

	@Override
	public int getShowLabelEveryUnits() {
		return this.showLabelEveryUnits;
	}

}
