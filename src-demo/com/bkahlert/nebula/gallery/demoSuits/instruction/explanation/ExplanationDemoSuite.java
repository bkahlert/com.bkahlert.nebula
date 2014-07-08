package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.list.ListExplanationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.normal.NormalExplanationDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.simple.SimpleExplanationDemoSuite;


@DemoSuite({ SimpleExplanationDemoSuite.class,
	ListExplanationDemoSuite.class, NormalExplanationDemoSuite.class })
@Demo
public class ExplanationDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
