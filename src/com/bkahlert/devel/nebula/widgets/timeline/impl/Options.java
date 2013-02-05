package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.security.InvalidParameterException;
import java.util.HashMap;

import com.bkahlert.devel.nebula.widgets.timeline.IOptions;

public class Options extends HashMap<String, Object> implements IOptions {

	private static final long serialVersionUID = 3104133404424383886L;

	@Override
	public void setTitle(String title) {
		this.put("title", title);
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
