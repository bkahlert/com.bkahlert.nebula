package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;

public class GoogleWebpage extends SingleFieldWebpage {

	private static final Logger LOGGER = Logger.getLogger(GoogleWebpage.class);

	private static URI uri = null;

	static {
		try {
			uri = new URI("http://www.google.de");
		} catch (URISyntaxException e) {
			LOGGER.fatal(e);
		}
	}

	public GoogleWebpage(Rectangle bounds, int timeout, String query) {
		super(uri, bounds, timeout, new ISelector.FieldSelector("q"), query,
				1000);
	}

}
