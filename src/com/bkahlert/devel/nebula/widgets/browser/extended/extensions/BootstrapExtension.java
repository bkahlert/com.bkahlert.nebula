package com.bkahlert.devel.nebula.widgets.browser.extended.extensions;

import java.util.ArrayList;
import java.util.Arrays;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;

public class BootstrapExtension extends BrowserCompositeExtension {

	@SuppressWarnings("unchecked")
	public BootstrapExtension() {
		super(
				"Bootstrap 3.0.0",
				"return (typeof jQuery !== 'undefined') && (typeof $().modal == 'function');",
				BrowserComposite.getFileUrl(BootstrapExtension.class,
						"bootstrap/js/bootstrap.min.js"), BrowserComposite
						.getFileUrl(BootstrapExtension.class,
								"bootstrap/css/bootstrap.min.css"),
				new ArrayList<Class<? extends IBrowserCompositeExtension>>(
						Arrays.asList(JQueryExtension.class)));
	}

}
