package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.scale.OrdinalScale;
import com.bkahlert.nebula.widgets.scale.OrdinalScale.IOrdinalScaleListener;

@Demo
public class OrdinalScaleDemo extends AbstractDemo {

	private OrdinalScale ordinalScale;

	@Override
	public void createDemo(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		// new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
		// .fillDefaults().grab(true, true).create());

		this.ordinalScale = new OrdinalScale(parent, SWT.BORDER, "Value #1",
				"Value #2", "Third Value");
		this.ordinalScale.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());
		this.ordinalScale.addListener(new IOrdinalScaleListener() {
			@Override
			public void valueChanged(String oldValue, String newValue) {
				// TODO Auto-generated method stub

			}

			@Override
			public void orderChanged(String[] oldOrdinals, String[] newOrdinals) {
				log("order changed:\n\tfrom: "
						+ new ArrayList<String>(Arrays.asList(oldOrdinals))
						+ "\n\tto: "
						+ new ArrayList<String>(Arrays.asList(newOrdinals)));
			}
		});

		this.ordinalScale.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_YELLOW));
		this.ordinalScale.setMargin(0);

		// new Label(parent, SWT.NONE).setLayoutData(GridDataFactory
		// .fillDefaults().grab(true, true).create());
	}
}
