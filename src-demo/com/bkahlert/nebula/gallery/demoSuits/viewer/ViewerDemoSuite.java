package com.bkahlert.nebula.gallery.demoSuits.viewer;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@DemoSuite({ FilterableViewerDemo.class, ProjectDisplayCompositeDemo.class,
		BaseProjectSelectionCompositeDemo.class,
		ProjectSelectionCompositeDemo.class })
@Demo
public class ViewerDemoSuite extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {

	}

}
