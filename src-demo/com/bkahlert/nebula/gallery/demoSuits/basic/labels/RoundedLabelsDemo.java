package com.bkahlert.nebula.gallery.demoSuits.basic.labels;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.RoundedLabels;

@Demo
public class RoundedLabelsDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		RoundedLabels roundedLabels = new RoundedLabels(parent, SWT.NONE,
				new RGB(200, 200, 200));
		roundedLabels.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		roundedLabels.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk" });

		RoundedLabels roundedLabels2 = new RoundedLabels(parent, SWT.BORDER,
				new RGB(200, 200, 200));
		roundedLabels2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		roundedLabels2.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk" });

		Composite wrapper = new Composite(parent, SWT.NONE);
		wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		wrapper.setLayout(GridLayoutFactory.swtDefaults().margins(1, 1)
				.spacing(2, 0).numColumns(2).create());
		Label label = new Label(wrapper, SWT.NONE);
		label.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4).create());
		label.setText("Filters:");
		RoundedLabels roundedLabels3 = new RoundedLabels(wrapper, SWT.NONE,
				new RGB(200, 200, 200));
		roundedLabels3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		roundedLabels3.setTexts(new String[] { "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj", "abc", "def",
				"kjkdjklsdjdsdslkjlkjlk", "kjkljlj" });

	}
}
