package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import java.util.ArrayList;
import java.util.Arrays;

import com.bkahlert.nebula.browser.BrowserUtils;

public class JQueryScrollToExtension extends BrowserCompositeExtension {

	@SuppressWarnings("unchecked")
	public JQueryScrollToExtension() {
		super(
				"jQuery scrollTo",
				"return typeof jQuery !== 'undefined' && typeof jQuery.scrollTo === 'function';",
				BrowserUtils.getFile(JQueryScrollToExtension.class,
						"jquery.scrollTo.js"),
				new ArrayList<Class<? extends IBrowserCompositeExtension>>(
						Arrays.asList(JQueryExtension.class)));
	}

}
