package com.bkahlert.nebula.screenshots.impl;

import org.eclipse.core.runtime.Assert;

import com.bkahlert.nebula.screenshots.IScreenshotCustomizer;
import com.bkahlert.nebula.screenshots.IScreenshotRequest;

public class ScreenshotRequest implements IScreenshotRequest {

	private FORMAT format;
	private IScreenshotCustomizer customizer;

	public ScreenshotRequest(FORMAT format, IScreenshotCustomizer customizer) {
		Assert.isLegal(format != null);
		this.format = format;
		this.customizer = customizer;
	}

	@Override
	public FORMAT getFormat() {
		return this.format;
	}

	@Override
	public IScreenshotCustomizer getCustomizer() {
		return this.customizer;
	}

}
