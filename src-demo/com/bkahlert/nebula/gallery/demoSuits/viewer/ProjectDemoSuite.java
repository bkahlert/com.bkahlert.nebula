package com.bkahlert.nebula.gallery.demoSuits.project;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;


@DemoSuite({ ProjectDisplayCompositeDemo.class,
	BaseProjectSelectionCompositeDemo.class,
	ProjectSelectionCompositeDemo.class })
@Demo
public class ProjectDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
