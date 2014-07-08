package com.bkahlert.nebula.screenshots.webpage;

import java.net.URI;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public interface IWebpage {

	public URI getUri();

	public Rectangle getBounds();

	public Point getDimensions();

	public Point getScrollPosition();

	public int getTimeout();

}
