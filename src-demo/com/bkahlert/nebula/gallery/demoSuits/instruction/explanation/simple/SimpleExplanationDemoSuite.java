package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;


@DemoSuite({ SimpleExplanationCompositeDemo.class,
	ExplanationOnlySimpleExplanationCompositeDemo.class,
	IconOnlySimpleExplanationCompositeDemo.class,
	HugeSimpleExplanationCompositeDemo.class })
@Demo
public class SimpleExplanationDemoSuite extends AbstractDemo {

    @Override
    public void createDemo(Composite parent) {

    }

}
