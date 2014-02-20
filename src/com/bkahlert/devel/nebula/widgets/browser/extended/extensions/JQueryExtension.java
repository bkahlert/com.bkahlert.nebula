package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import com.bkahlert.nebula.browser.BrowserUtils;

public class JQueryExtension extends BrowserCompositeExtension {

	public JQueryExtension() {
		super("jQuery 1.9.0", "return typeof jQuery !== 'undefined';",
				BrowserUtils.getFile(JQueryExtension.class, "jquery-1.9.0.js"),
				null);
	}

}
