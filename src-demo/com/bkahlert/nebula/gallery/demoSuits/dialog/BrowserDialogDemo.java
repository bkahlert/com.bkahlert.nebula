package com.bkahlert.nebula.gallery.demoSuits.dialog;

import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.dialogs.BrowserDialog;
import com.bkahlert.nebula.dialogs.BrowserException;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class BrowserDialogDemo extends AbstractDemo {

	@Override
	public void createControls(final Composite composite) {
		Button openPage = new Button(composite, SWT.PUSH);
		openPage.setText("Open Page");
		openPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					BrowserDialog browserDialog = new BrowserDialog(new Shell());
					browserDialog.open("http://validator.w3.org", new Point(
							800, 600), new Point(50, 100), 3000,
							new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									log("Page opened, resized and scrolled");
									return null;
								}
							});
				} catch (BrowserException e1) {
					log(e1.getMessage());
				}
			}
		});
	}

}
