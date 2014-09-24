package com.bkahlert.nebula.gallery.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.bkahlert.nebula.gallery.ImageManager;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoElement;
import com.bkahlert.nebula.gallery.demoExplorer.DemoExplorer;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;
import com.bkahlert.nebula.gallery.widgets.DemoBannerComposite;
import com.bkahlert.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

public class GalleryView extends ViewPart {
	public static final String ID = "com.bkahlert.nebula.gallery.views.GalleryView";
	public static SelectionProviderIntermediate selectionProviderIntermediate = new SelectionProviderIntermediate();

	protected DemoExplorer demoExplorer;
	protected Composite demoArea;
	protected DemoBannerComposite demoAreaBanner;
	protected Composite demoAreaContent;
	protected AbstractDemo currentDemo;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		this.getSite().setSelectionProvider(selectionProviderIntermediate);

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL | SWT.FLAT);
		sashForm.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.create());
		sashForm.setLayout(new FillLayout());

		this.createDemoExplorer(sashForm);
		this.createDemoArea(sashForm);
		sashForm.setSashWidth(5);
		sashForm.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		sashForm.setWeights(new int[] { 30, 70 });

		this.openDemo();
	}

	protected void createDemoExplorer(SashForm sashForm) {
		this.demoExplorer = new DemoExplorer(sashForm, SWT.NONE);
		this.demoExplorer.getViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						GalleryView.this.openDemo();
					}
				});
	}

	protected void createDemoArea(SashForm sashForm) {
		this.demoArea = new Composite(sashForm, SWT.NONE);
		this.demoArea.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		/*
		 * Headline
		 */
		this.demoAreaBanner = new DemoBannerComposite(this.demoArea, SWT.NONE);
		this.demoAreaBanner.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING,
				true, false));

		/*
		 * Content
		 */
		this.demoAreaContent = new Composite(this.demoArea, SWT.NONE);
		this.demoAreaContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
	}

	public void openDemo() {
		DemoElement demoElement = this.demoExplorer.getSelectedDemoElement();
		if (demoElement == null) {
			return;
		}

		Demo meta = demoElement.getDemo().getAnnotation(Demo.class);
		String title = (meta != null && !meta.title().isEmpty()) ? meta.title()
				: "Demo: " + demoElement.getStyledText().toString();
		if (!meta.description().isEmpty()) {
			title += "\n" + meta.description();
		}
		this.demoAreaBanner.setContent(new IllustratedText(ImageManager.DEMO,
				title));

		if (this.currentDemo != null) {
			this.currentDemo.dispose();
		}
		CompositeUtils.emptyComposite(this.demoAreaContent);

		try {
			this.currentDemo = demoElement.getDemo().newInstance();
			this.currentDemo.createPartControls(this.demoAreaContent);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.demoArea.layout();
	}

	@Override
	public void setFocus() {
		// do nothing
	}
}