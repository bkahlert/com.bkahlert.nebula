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

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;
import com.bkahlert.nebula.gallery.ImageManager;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoExplorer.DemoElement;
import com.bkahlert.nebula.gallery.demoExplorer.DemoExplorer;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;
import com.bkahlert.nebula.gallery.widgets.BannerComposite;
import com.bkahlert.nebula.gallery.widgets.DemoBannerComposite;

public class GalleryView extends ViewPart {
	public static final String ID = "com.bkahlert.nebula.gallery.views.GalleryView";
	public static SelectionProviderIntermediate selectionProviderIntermediate = new SelectionProviderIntermediate();

	protected DemoExplorer demoExplorer;
	protected DemoBannerComposite demoBannerComposite;
	protected Composite demoComposite;
	protected Composite content;
	protected AbstractDemo currentDemo;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		this.getSite().setSelectionProvider(selectionProviderIntermediate);

		// createBanner(parent);

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

	protected void createBanner(final Composite parent) {
		BannerComposite bannerComposite = new BannerComposite(parent, SWT.NONE);
		bannerComposite.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		bannerComposite.setContent(new IllustratedText(
				ImageManager.WIDGET_GALLERY_32, "Saros Widget Gallery"));
	}

	protected DemoExplorer createDemoExplorer(SashForm sashForm) {
		this.demoExplorer = new DemoExplorer(sashForm, SWT.NONE);
		this.demoExplorer.getViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						GalleryView.this.openDemo();
					}
				});
		return this.demoExplorer;
	}

	protected void createDemoArea(SashForm sashForm) {
		this.demoComposite = new Composite(sashForm, SWT.NONE);
		this.demoComposite.setLayout(GridLayoutFactory.fillDefaults()
				.spacing(0, 0).create());

		/*
		 * Headline
		 */
		Composite headline = this.createHeadline(this.demoComposite);
		headline.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));

		/*
		 * Content
		 */
		this.content = new Composite(this.demoComposite, SWT.NONE);
		this.content
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * Creates the controls for this demo
	 * 
	 * @param composite
	 * @return
	 */
	public Composite createHeadline(Composite composite) {
		this.demoBannerComposite = new DemoBannerComposite(composite, SWT.NONE);
		this.demoBannerComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.BEGINNING, true, false));

		return this.demoBannerComposite;
	}

	public void openDemo() {
		DemoElement demoElement = this.demoExplorer.getSelectedDemoElement();
		if (demoElement == null) {
			return;
		}

		Demo meta = demoElement.getDemo().getAnnotation(Demo.class);
		String title = (meta != null && !meta.value().isEmpty()) ? meta.value()
				: "Demo: " + demoElement.getStyledText().toString();
		this.demoBannerComposite.setContent(new IllustratedText(
				ImageManager.DEMO, title));
		this.demoComposite.layout();

		if (this.currentDemo != null) {
			this.currentDemo.dispose();
		}
		CompositeUtils.emptyComposite(this.content);

		try {
			this.currentDemo = demoElement.getDemo().newInstance();
			this.currentDemo.createPartControls(this.content);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		// do nothing
	}
}