package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.widgets.explanation.ExplanationComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class IconOnlyExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		new ExplanationComposite(parent, SWT.NONE, SWT.ICON_INFORMATION);
	}
}
