package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class BrowserDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new FillLayout());

		BrowserComposite browserComposite = new BrowserComposite(parent,
				SWT.NONE, "http://lab.simurai.com/tilt-shift/") {
		};
		log(browserComposite.getBrowser().getUrl());
	}
}
