package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Rectangle;


public class GoogleScreenshotRequest extends WebpageScreenshotRequest {

	private static final Logger LOGGER = Logger
			.getLogger(GoogleScreenshotRequest.class);

	private static URI uri = null;

	static {
		try {
			uri = new URI("http://www.google.de");
		} catch (URISyntaxException e) {
			LOGGER.fatal(e);
		}
	}

	public GoogleScreenshotRequest(FORMAT format, Rectangle bounds,
			final String query, final int timeout) {
		super(format, uri, bounds, new SearchFormFiller(query), timeout);

	}

}
