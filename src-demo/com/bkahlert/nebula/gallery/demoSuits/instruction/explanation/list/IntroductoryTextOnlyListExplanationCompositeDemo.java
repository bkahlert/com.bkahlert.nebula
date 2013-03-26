package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class IntroductoryTextOnlyListExplanationCompositeDemo extends
		AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, "I'm the introductory text...");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
