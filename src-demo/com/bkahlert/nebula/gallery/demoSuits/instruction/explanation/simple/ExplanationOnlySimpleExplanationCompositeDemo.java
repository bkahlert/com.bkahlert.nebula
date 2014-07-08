package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.SimpleExplanationComposite;
import com.bkahlert.nebula.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;

@Demo
public class ExplanationOnlySimpleExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		SimpleExplanationComposite simpleExplanationComposite = new SimpleExplanationComposite(
				parent, SWT.NONE);
		SimpleExplanation simpleExplanation = new SimpleExplanation(
				"This is a simple explanation.");
		simpleExplanationComposite.setExplanation(simpleExplanation);
	}
}
