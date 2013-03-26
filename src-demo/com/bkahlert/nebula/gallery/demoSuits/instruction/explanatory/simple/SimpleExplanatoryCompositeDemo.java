package com.bkahlert.nebula.gallery.demoSuits.instruction.explanatory.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


import com.bkahlert.devel.nebula.widgets.explanation.SimpleExplanationComposite.SimpleExplanation;
import com.bkahlert.devel.nebula.widgets.explanation.explanatory.SimpleExplanatoryComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class SimpleExplanatoryCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		final SimpleExplanatoryComposite explanatoryComposite = new SimpleExplanatoryComposite(
				parent, SWT.NONE);

		Button contentControl = new Button(explanatoryComposite, SWT.NONE);
		explanatoryComposite.setContentControl(contentControl);
		contentControl.setText("Show the simple explanation...");
		contentControl.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int icon = SWT.ICON_WORKING;
				String text = "I tell you how to use this composite.\n"
						+ "This message closes in 5 seconds.";
				SimpleExplanation expl = new SimpleExplanation(icon, text);
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
