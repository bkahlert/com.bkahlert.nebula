package com.bkahlert.nebula.widgets.browser.extended.extensions.jquery;

import java.util.Arrays;

import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.extensions.BrowserExtension;

public class JQueryBrowserExtension extends BrowserExtension {

	public JQueryBrowserExtension() {
		super("jQuery 1.9.0", "return typeof jQuery !== 'undefined';", Arrays
				.asList(BrowserUtils.getFile(JQueryBrowserExtension.class,
						"jquery-1.9.0.js")), null, null);
	}

}
