package com.bkahlert.nebula.gallery.demoSuits.viewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.viewer.FilterableTreeViewer;

@Demo
public class FilterableViewerDemo extends AbstractDemo {
	protected FilterableTreeViewer filterableTreeViewer;

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new FillLayout());

		this.filterableTreeViewer = new FilterableTreeViewer(parent, SWT.BORDER);
	}
}
