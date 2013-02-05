package com.bkahlert.devel.nebula.widgets.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class BrowserComposite extends Composite implements IBrowserComposite {

	private Browser browser;

	public BrowserComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.browser = new Browser(this, SWT.NONE);
	}

	@Override
	public Browser getBrowser() {
		return this.browser;
	}

}
