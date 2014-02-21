package com.bkahlert.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.bootstrap.BootstrapBrowserExtension;

public class BootstrapBrowser extends JQueryBrowser implements
		IBootstrapBrowser {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(BootstrapBrowser.class);

	public BootstrapBrowser(Composite parent, int style) {
		this(parent, style, new IBrowserExtension[] {});
	}

	@SuppressWarnings("serial")
	public BootstrapBrowser(Composite parent, int style,
			final IBrowserExtension[] extensions) {
		super(parent, style, new ArrayList<IBrowserExtension>() {
			{
				this.add(new BootstrapBrowserExtension());
				if (extensions != null) {
					this.addAll(Arrays.asList(extensions));
				}
			}
		}.toArray(new IBrowserExtension[0]));
	}

}
