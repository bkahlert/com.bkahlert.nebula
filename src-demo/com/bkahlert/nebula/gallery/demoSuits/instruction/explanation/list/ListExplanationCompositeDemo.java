package com.bkahlert.nebula.gallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;

@Demo
public class ListExplanationCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		ListExplanationComposite listExplanationComposite = new ListExplanationComposite(
				parent, SWT.NONE);
		ListExplanation listItemExplanation = new ListExplanation(
				SWT.ICON_INFORMATION, "I'm the introductory text...",
				"BootstrapBrowser item 1", "BootstrapBrowser item 2", "BootstrapBrowser item 3", "BootstrapBrowser item 4",
				"BootstrapBrowser item 5", "BootstrapBrowser item 6", "BootstrapBrowser item 7", "BootstrapBrowser item 8",
				"BootstrapBrowser item 9", "BootstrapBrowser item 10", "BootstrapBrowser item 11", "BootstrapBrowser item 12",
				"BootstrapBrowser item 13", "BootstrapBrowser item 14", "BootstrapBrowser item 15");
		listExplanationComposite.setExplanation(listItemExplanation);
	}
}
