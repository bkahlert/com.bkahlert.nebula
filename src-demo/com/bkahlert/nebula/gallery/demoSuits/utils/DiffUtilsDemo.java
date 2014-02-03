package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.decoration.EmptyText;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.DiffUtils;

@Demo
public class DiffUtilsDemo extends AbstractDemo {

	private Text source;
	private Text patch;
	private Text output;

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(2)
				.spacing(0, 0).create());

		this.source = new Text(parent, SWT.BORDER | SWT.MULTI);
		this.source.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		new EmptyText(this.source, "Source");

		this.patch = new Text(parent, SWT.BORDER | SWT.MULTI);
		this.patch.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		new EmptyText(this.patch, "Patch");

		this.output = new Text(parent, SWT.BORDER | SWT.MULTI);
		this.output.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).span(2, 1).create());
		new EmptyText(this.output, "Output");
	}

	@Override
	public void createControls(Composite composite) {
		Button testDataButton = new Button(composite, SWT.PUSH);
		testDataButton.setText("Load Test Data");
		testDataButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiffUtilsDemo.this.source
						.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr,\n"
								+ "sed diam nonumy eirmod tempor invidunt ut labore et dolore\n"
								+ "magna aliquyam erat, sed diam voluptua.");
				DiffUtilsDemo.this.patch
						.setText("--- 1.txt	2013-04-06 01:27:59.000000000 +0200\n"
								+ "+++ 2.txt	2013-04-06 01:28:20.000000000 +0200\n"
								+ "@@ -1,3 +1,3 @@\n"
								+ " Lorem ipsum dolor sit amet, consetetur sadipscing elitr,\n"
								+ "-sed diam nonumy eirmod tempor invidunt ut labore et dolore\n"
								+ "-magna aliquyam erat, sed diam voluptua.\n"
								+ "\\ No newline at end of file\n"
								+ "+sed diam tempor invidunt ut labore et dolore\n"
								+ "+magna aliquyam erat, nonumy eirmod sed diam voluptua.\n"
								+ "\\ No newline at end of file)");
			}
		});

		Button patchButton = new Button(composite, SWT.PUSH);
		patchButton.setText("Patch Source");
		patchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String source = DiffUtilsDemo.this.source.getText();
				final String patch = DiffUtilsDemo.this.patch.getText();
				Future<String> output = ExecutorUtil
						.nonUISyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								return DiffUtils.patch(source, patch);
							}
						});
				try {
					DiffUtilsDemo.this.output.setText(output.get());
				} catch (InterruptedException e1) {
					log(e1.getMessage());
				} catch (ExecutionException e1) {
					log(e1.getMessage());
				}
				log("Successfully patched");
			}
		});
	}
}
