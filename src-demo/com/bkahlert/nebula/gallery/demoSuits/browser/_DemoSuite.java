package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@DemoSuite(value = { BrowserCompositeDemo.class,
		MultipleBrowserCompositeDemo.class, ComposerDemo.class,
		ComposerReadOnlyDemo.class, EditorDemo.class,
		MultipleEditorsDemo.class, ImageDemo.class, BaseTimelineDemo.class,
		TimelineDemo.class, TimelineAndComposerAndEditorDemo.class,
		TimelineGroupDemo.class, MinimalTimelineGroupViewerDemo.class,
		ExtendedBrowserDemo.class, BootstrapBrowserDemo.class })
@Demo
public class _DemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		return;
	}
}
