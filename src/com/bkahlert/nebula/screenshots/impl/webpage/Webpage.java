package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.nebula.screenshots.webpage.IWebpage;

public class Webpage implements IWebpage {

	private URI uri;
	private Rectangle bounds;
	private int timeout;

	public Webpage(URI uri, Rectangle bounds, int timeout) {
		Assert.isLegal(uri != null && bounds != null);
		this.uri = uri;
		this.bounds = bounds;
		this.timeout = timeout;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public Rectangle getBounds() {
		return this.bounds;
	}

	@Override
	public Point getDimensions() {
		return new Point(this.bounds.width, this.bounds.height);
	}

	@Override
	public Point getScrollPosition() {
		return new Point(this.bounds.x, this.bounds.y);
	}

	@Override
	public int getTimeout() {
		return this.timeout;
	}

}
