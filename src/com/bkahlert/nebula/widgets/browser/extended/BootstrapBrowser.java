package com.bkahlert.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.browser.extended.extensions.BootstrapExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;

public class BootstrapBrowser extends JQueryBrowser implements
		IBootstrapBrowser {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(BootstrapBrowser.class);

	public BootstrapBrowser(Composite parent, int style) {
		this(parent, style, new IBrowserCompositeExtension[] {});
	}

	@SuppressWarnings("serial")
	public BootstrapBrowser(Composite parent, int style,
			final IBrowserCompositeExtension[] extensions) {
		super(parent, style, new ArrayList<IBrowserCompositeExtension>() {
			{
				this.add(new BootstrapExtension());
				if (extensions != null) {
					this.addAll(Arrays.asList(extensions));
				}
			}
		}.toArray(new IBrowserCompositeExtension[0]));
	}

}
