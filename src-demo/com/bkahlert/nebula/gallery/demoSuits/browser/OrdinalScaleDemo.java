package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.scale.OrdinalScale;
import com.bkahlert.nebula.widgets.scale.OrdinalScale.EditType;
import com.bkahlert.nebula.widgets.scale.OrdinalScale.OrdinalScaleAdapter;

@Demo
public class OrdinalScaleDemo extends AbstractDemo {

	private OrdinalScale ordinalScale1;
	private OrdinalScale ordinalScale2;

	@Override
	public void createControls(Composite composite) {
		Button getOrdinals = new Button(composite, SWT.PUSH);
		getOrdinals.setText("Get Ordinals");
		getOrdinals.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				log(new ArrayList<String>(Arrays
						.asList(OrdinalScaleDemo.this.ordinalScale1
								.getOrdinals())).toString());
			}
		});

		Button deselect = new Button(composite, SWT.PUSH);
		deselect.setText("Deselect");
		deselect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OrdinalScaleDemo.this.ordinalScale2.setValue(null);
			}
		});

		Button select = new Button(composite, SWT.PUSH);
		select.setText("Select \"Third Value\"");
		select.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OrdinalScaleDemo.this.ordinalScale2.setValue("Third Value");
			}
		});
	}

	private void syncScales() {
		String backupValue = this.ordinalScale2.getValue();
		try {
			this.ordinalScale2.setOrdinals(this.ordinalScale1.getOrdinals())
					.get();
			this.ordinalScale2.setValue(backupValue).get();
		} catch (Exception e) {
			log(e);
		}
	}

	@Override
	public void createDemo(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().numColumns(3)
				.create());

		// Label label1 = new Label(parent, SWT.WRAP);
		// label1.setLayoutData(GridDataFactory.fillDefaults().grab(false,
		// false)
		// .create());
		// label1.setText("Change the order of the ordinals here:");
		//
		// Label label2 = new Label(parent, SWT.WRAP);
		// label2.setLayoutData(GridDataFactory.fillDefaults().grab(false,
		// false)
		// .create());
		// label2.setText("Change the value of the ordinal scale here:");
		//
		// new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
		// .fillDefaults().grab(true, false).create());

		String[] ordinals = new String[] { "Value #1", "Value #2",
				"Third Value" };

		this.ordinalScale1 = new OrdinalScale(parent, SWT.BORDER,
				EditType.CHANGE_ORDER);
		this.ordinalScale1.setLayoutData(GridDataFactory.fillDefaults()
				.create());
		this.ordinalScale1.addListener(new OrdinalScaleAdapter() {
			@Override
			public void orderChanged(String[] oldOrdinals, String[] newOrdinals) {
				OrdinalScaleDemo.this.syncScales();
				log("order changed:\n\tfrom: "
						+ new ArrayList<String>(Arrays.asList(oldOrdinals))
						+ "\n\tto: "
						+ new ArrayList<String>(Arrays.asList(newOrdinals)));
			}

			@Override
			public void ordinalAdded(String newOrdinal) {
				OrdinalScaleDemo.this.syncScales();
				log("ordinal added: " + newOrdinal);
			}

			@Override
			public void ordinalRemoved(String ordinal) {
				OrdinalScaleDemo.this.syncScales();
				log("ordinal removed: " + ordinal);
			}

			@Override
			public void ordinalRenamed(String oldName, String newName) {
				OrdinalScaleDemo.this.syncScales();
				log("ordinal renamed: " + oldName + " -> " + newName);
			}
		});
		this.ordinalScale1.setMargin(0);
		this.ordinalScale1.setOrdinals(ordinals);

		this.ordinalScale2 = new OrdinalScale(parent, SWT.BORDER,
				EditType.CHANGE_VALUE);
		this.ordinalScale2.setLayoutData(GridDataFactory.fillDefaults()
				.create());
		this.ordinalScale2.addListener(new OrdinalScaleAdapter() {
			@Override
			public void valueChanged(String oldValue, String newValue) {
				log("value changed:\n\tfrom: " + oldValue + "\n\tto: "
						+ newValue);
			}
		});

		this.ordinalScale2.setMargin(0);
		this.ordinalScale2.setOrdinals(ordinals);

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().grab(true, false).create());

		new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
				.fillDefaults().span(3, 1).grab(true, true).create());
	}
}
