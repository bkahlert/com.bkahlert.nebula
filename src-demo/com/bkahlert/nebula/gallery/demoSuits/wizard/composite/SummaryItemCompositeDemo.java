package com.bkahlert.nebula.gallery.demoSuits.wizard.composite;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.utils.LayoutUtils;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.devel.nebula.widgets.wizards.SummaryItemComposite;
import com.bkahlert.nebula.gallery.ImageManager;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.StringUtils;

@Demo
public class SummaryItemCompositeDemo extends AbstractDemo {
	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite root = new Composite(parent, SWT.NONE);
		root.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		root.setLayout(LayoutUtils.createGridLayout(0, 10));
		Image[] images = new Image[] { ImageManager.DEMO,
				ImageManager.DEMO_SUITE, ImageManager.RELOAD };

		for (Image image : images) {
			SummaryItemComposite summaryItemCompositeDemo = new SummaryItemComposite(
					root, SWT.NONE);
			summaryItemCompositeDemo.setLayoutData(new GridData(SWT.FILL,
					SWT.CENTER, true, false));
			summaryItemCompositeDemo.setContent(new IllustratedText(image,
					"Summary title\nSummary demoAreaContent: "
							+ StringUtils.genRandom(32)));
		}
	}
}
