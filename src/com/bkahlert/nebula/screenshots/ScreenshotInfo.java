package com.bkahlert.nebula.screenshots;

import org.eclipse.swt.graphics.Point;

public class ScreenshotInfo {
	private String url;
	private Point scrollPosition;
	private Point windowDimensions;

	public ScreenshotInfo(String url, Point scrollPosition, Point windowDimensions) {
		super();
		this.url = url;
		this.scrollPosition = scrollPosition;
		this.windowDimensions = windowDimensions;
	}

	public String getUrl() {
		return url;
	}

	public Point getScrollPosition() {
		return scrollPosition;
	}

	public Point getWindowDimensions() {
		return windowDimensions;
	}

}
