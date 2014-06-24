package com.bkahlert.nebula.gallery.demoSuits.basic.loader;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.loader.LoaderComposite;

@Demo
public class LoaderCompositeDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite composite) {
		composite.setBackground(ColorUtils.createRandomColor());
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(10, 10)
				.equalWidth(true).spacing(10, 10).numColumns(6).create());

		LoaderComposite lc1 = new LoaderComposite(composite);
		lc1.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(6, 2).create());

		LoaderComposite lc2 = new LoaderComposite(composite);
		lc2.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(2, 2).create());

		LoaderComposite lc3 = new LoaderComposite(composite);
		lc3.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(4, 4).create());

		LoaderComposite lc4 = new LoaderComposite(composite);
		lc4.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 2).create());

		LoaderComposite lc5 = new LoaderComposite(composite);
		lc5.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());

		LoaderComposite lc6 = new LoaderComposite(composite);
		lc6.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 1).create());

	}

}
