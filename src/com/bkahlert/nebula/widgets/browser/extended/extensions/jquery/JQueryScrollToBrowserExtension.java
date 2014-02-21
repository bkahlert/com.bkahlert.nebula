package com.bkahlert.nebula.widgets.browser.extended.extensions.jquery;

import java.util.ArrayList;
import java.util.Arrays;

import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.extensions.BrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;

public class JQueryScrollToBrowserExtension extends BrowserExtension {

	@SuppressWarnings("unchecked")
	public JQueryScrollToBrowserExtension() {
		super(
				"jQuery scrollTo",
				"return typeof jQuery !== 'undefined' && typeof jQuery.scrollTo === 'function';",
				BrowserUtils.getFile(JQueryScrollToBrowserExtension.class,
						"jquery.scrollTo.js"),
				new ArrayList<Class<? extends IBrowserExtension>>(
						Arrays.asList(JQueryBrowserExtension.class)));
	}

}
