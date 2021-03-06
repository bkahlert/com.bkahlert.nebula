package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.browser.jointjs.JointJSDemo;
import com.bkahlert.nebula.gallery.demoSuits.browser.jointjs.JointJSWithInformationDemo;
import com.bkahlert.nebula.gallery.demoSuits.browser.timeline.TimelineDemoSuite;

@DemoSuite(value = { BrowserDemo.class, MultipleBrowsersDemo.class,
		ItemListDemo.class, ItemListViewerDemo.class, OrdinalScaleDemo.class,
		ComposerDemo.class, ComposerReadOnlyDemo.class, EditorDemo.class,
		MultipleEditorsDemo.class, ImageDemo.class, TimelineDemoSuite.class,
		JQueryBrowserDemo.class, DragnDropBrowserDemo.class,
		BootstrapBrowserDemo.class, JointJSDemo.class,
		JointJSWithInformationDemo.class })
@Demo
public class BrowserDemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		return;
	}
}
