package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.SimpleExplanationComposite;
import com.bkahlert.nebula.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

@Demo
public class IconOnlySimpleExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		SimpleExplanationComposite simpleExplanationComposite = new SimpleExplanationComposite(
				parent, SWT.NONE);
		SimpleExplanation simpleExplanation = new SimpleExplanation(
				SWT.ICON_INFORMATION);
		simpleExplanationComposite.setExplanation(simpleExplanation);
	}
}
