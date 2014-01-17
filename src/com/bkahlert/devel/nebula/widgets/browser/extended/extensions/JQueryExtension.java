package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;

public class JQueryExtension extends BrowserCompositeExtension {

	public JQueryExtension() {
		super("jQuery 1.9.0", "return typeof jQuery !== 'undefined';",
				BrowserComposite.getFileUrl(JQueryExtension.class,
						"jquery-1.9.0.js"), null);
	}

}
