package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.AutosaveEditor;
import com.bkahlert.devel.nebula.widgets.editor.Editor;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class MultipleEditorsDemo extends AbstractDemo {

	private static Editor<Object> createEditor(Composite composite, final int i) {
		return new AutosaveEditor<Object>(composite, SWT.NONE, 500,
				ToolbarSet.TERMINAL) {
			@Override
			public String getHtml(Object objectToLoad, IProgressMonitor monitor) {
				MultipleEditorsDemo.log("Editor #" + i + " loaded: "
						+ objectToLoad);
				return objectToLoad.toString();
			}

			@Override
			public void setHtml(Object objectToLoad, String html,
					IProgressMonitor monitor) {
				MultipleEditorsDemo.log("Editor #" + i + " saved: " + html);
			}
		};
	}

	private static FillLayout setMarginAndSpace(FillLayout fillLayout, int value) {
		fillLayout.marginWidth = fillLayout.marginHeight = fillLayout.spacing = value;
		return fillLayout;
	}

	private Object syncedInput;
	private Editor<Object> independentEditor;

	@Override
	public void createDemo(Composite composite) {
		composite
				.setLayout(setMarginAndSpace(new FillLayout(SWT.VERTICAL), 10));

		Composite group1 = new RoundedComposite(composite, SWT.BORDER);
		group1.setBackground(ColorUtils.createRandomColor());
		group1.setLayout(setMarginAndSpace(new FillLayout(SWT.HORIZONTAL), 10));
		Object input = "<p>Shared input... simply type and see how all editors reflect the changes.</p>";
		createEditor(group1, 1).load(input);
		createEditor(group1, 2).load(input);
		createEditor(group1, 3).load(input);

		Composite group2 = new RoundedComposite(composite, SWT.BORDER);
		group2.setBackground(ColorUtils.createRandomColor());
		group2.setLayout(setMarginAndSpace(new FillLayout(SWT.HORIZONTAL), 10));
		this.syncedInput = "<p>Group 2...</p>";
		createEditor(group2, 4).load(this.syncedInput);
		this.independentEditor = createEditor(group2, 5);
		this.independentEditor.load(this.syncedInput);
		createEditor(group2, 6).load(this.syncedInput);
	}

	@Override
	public void createControls(Composite composite) {
		Button button = new Button(composite, SWT.PUSH);
		button.setText("Set to random input");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MultipleEditorsDemo.this.independentEditor.load("Random");
			}
		});

		Button button2 = new Button(composite, SWT.PUSH);
		button2.setText("Set to synced input");
		button2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MultipleEditorsDemo.this.independentEditor
						.load(MultipleEditorsDemo.this.syncedInput);
			}
		});
	}
}
