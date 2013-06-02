package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.nebula.screenshots.impl.ScreenshotRequest;
import com.bkahlert.nebula.screenshots.webpage.IWebpageFormFiller;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;

public class WebpageScreenshotRequest extends ScreenshotRequest implements
		IWebpageScreenshotRequest {

	private URI uri;
	private Rectangle bounds;
	private IWebpageFormFiller webpageFormFiller;
	private int timeout;

	public WebpageScreenshotRequest(FORMAT format, URI uri, Rectangle bounds,
			IWebpageFormFiller webpageFormFiller, int timeout) {
		super(format);
		Assert.isLegal(uri != null && bounds != null);
		this.uri = uri;
		this.bounds = bounds;
		this.webpageFormFiller = webpageFormFiller;
		this.timeout = timeout;
	}

	public WebpageScreenshotRequest(FORMAT format, URI uri, Rectangle bounds,
			int timeout) {
		this(format, uri, bounds, null, timeout);
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
	public IWebpageFormFiller getFormFiller() {
		return this.webpageFormFiller;
	}

	@Override
	public int getTimeout() {
		return this.timeout;
	}

}
