package com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.list.ListExplanatoryDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.normal.NormalExplanatoryDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.simple.SimpleExplanatoryDemoSuite;


@DemoSuite({ SimpleExplanatoryDemoSuite.class,
	ListExplanatoryDemoSuite.class, NormalExplanatoryDemoSuite.class })
@Demo
public class ExplanatoryDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
