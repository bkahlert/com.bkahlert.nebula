package com.bkahlert.nebula.gallery.demoSuits.basic.labels;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.widgets.RoundedLabel;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class RoundedLabelDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite composite) {
		composite.setLayout(GridLayoutFactory.swtDefaults().create());

		RoundedLabel label1 = new RoundedLabel(composite, SWT.NONE);
		label1.setText("SWT.NONE");

		RoundedLabel label2 = new RoundedLabel(composite, SWT.BORDER);
		label2.setText("SWT.BORDER");

		RoundedLabel label3 = new RoundedLabel(composite, SWT.BORDER);
		label3.setBackground(ColorUtils.createRandomColor());
		label3.setText("SWT.BORDER | Random Background Color");

	}

}
