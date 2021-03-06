package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;

@Demo
public class ItemsOnlyListExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, null, "BootstrapBrowser item 1", "BootstrapBrowser item 2",
				"BootstrapBrowser item 3");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
