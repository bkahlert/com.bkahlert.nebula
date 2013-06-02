package com.bkahlert.nebula.screenshots.impl;

import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.screenshots.IScreenshotRequest;

public class ScreenshotRequest implements IScreenshotRequest {

	private FORMAT format;

	public ScreenshotRequest(FORMAT format) {
		Assert.isLegal(format != null);
		this.format = format;
	}

	@Override
	public FORMAT getFormat() {
		return this.format;
	}

}
