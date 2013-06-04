package com.bkahlert.nebula.screenshots.webpage;

import java.net.URI;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.nebula.screenshots.IScreenshotRequest;

public interface IWebpageScreenshotRequest extends IScreenshotRequest {

	public URI getUri();

	public Rectangle getBounds();

	public Point getDimensions();

	public Point getScrollPosition();

	public int getTimeout();

}
