package com.bkahlert.nebula.gallery.demoSuits.basic;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.basic.illustrated.IllustratedCompositeDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.basic.labels.RoundedLabelsDemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.basic.rounded.RoundedCompositeDemoSuite;

@DemoSuite({ RoundedCompositeDemoSuite.class,
		IllustratedCompositeDemoSuite.class, RoundedLabelsDemoSuite.class })
public class BasicDemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {

	}
}
