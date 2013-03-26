package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.widgets.explanation.ExplanationComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class ExplanationOnlyExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		final ExplanationComposite expl = new ExplanationComposite(parent,
				SWT.NONE, null);
		expl.setLayout(new FillLayout());
		Button explContent_hide = new Button(expl, SWT.PUSH);
		explContent_hide.setText("I'm a button explanation.");
	}
}
