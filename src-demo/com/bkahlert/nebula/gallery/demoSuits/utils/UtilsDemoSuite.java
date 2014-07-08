package com.bkahlert.nebula.gallery.demoSuits.utils;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoSuite;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@DemoSuite(value = { ColorUtilsDemo.class, DiffUtilsDemo.class })
@Demo
public class UtilsDemoSuite extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		return;
	}
}
