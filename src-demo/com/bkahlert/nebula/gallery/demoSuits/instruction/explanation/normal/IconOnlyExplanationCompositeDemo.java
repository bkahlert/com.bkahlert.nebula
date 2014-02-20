package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.ExplanationComposite;

@Demo
public class IconOnlyExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		new ExplanationComposite(parent, SWT.NONE, SWT.ICON_INFORMATION);
	}
}
