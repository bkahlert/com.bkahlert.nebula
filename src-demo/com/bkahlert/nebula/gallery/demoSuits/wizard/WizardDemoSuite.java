package com.bkahlert.nebula.gallery.demoSuits.wizard;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.wizard.composite.CompositeDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.wizard.pages.PagesDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.wizard.wizards.AllWizardsDemo;


@DemoSuite({ CompositeDemoSuite.class, PagesDemoSuite.class,
	AllWizardsDemo.class })
@Demo
public class WizardDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
