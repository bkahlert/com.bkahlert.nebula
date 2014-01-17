package com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.list;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;
import com.bkahlert.devel.nebula.widgets.explanation.explanatory.ListExplanatoryComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class ListExplanatoryCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		final ListExplanatoryComposite explanatoryComposite = new ListExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the list explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int icon = SWT.ICON_WORKING;
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				ListExplanation expl = new ListExplanation(icon, text,
						"BootstrapBrowser item 1", "BootstrapBrowser item 2", "BootstrapBrowser item 3",
						"BootstrapBrowser item 4", "BootstrapBrowser item 5", "BootstrapBrowser item 6",
						"BootstrapBrowser item 7", "BootstrapBrowser item 8", "BootstrapBrowser item 9",
						"BootstrapBrowser item 10", "BootstrapBrowser item 11", "BootstrapBrowser item 12",
						"BootstrapBrowser item 13", "BootstrapBrowser item 14", "BootstrapBrowser item 15");
				explanatoryComposite.showExplanation(expl);

				Display.getCurrent().timerExec(5000, new Runnable() {

					@Override
					public void run() {
						explanatoryComposite.hideExplanation();
					}

				});
			}

		});
	}
}
