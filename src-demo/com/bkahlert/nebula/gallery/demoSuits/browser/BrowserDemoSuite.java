package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@DemoSuite(value = { BrowserDemo.class, ComposerDemo.class, EditorDemo.class,
		ImageDemo.class, TimelineDemo.class })
@Demo
public class BrowserDemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		return;
	}
}
