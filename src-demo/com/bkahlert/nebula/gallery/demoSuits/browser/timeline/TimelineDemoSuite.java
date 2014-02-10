package com.bkahlert.nebula.gallery.demoSuits.browser.timeline;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@DemoSuite(value = { BaseTimelineDemo.class, TimelineDemo.class,
		TimelineAndComposerAndEditorDemo.class, TimelineGroupDemo.class,
		MinimalTimelineGroupViewerDemo.class, TimelineGroupViewerDemo.class })
@Demo
public class TimelineDemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		return;
	}
}
