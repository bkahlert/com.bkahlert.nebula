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

		new BrowserComposite(parent, SWT.NONE, "http://google.com") {
		};
	}
}
