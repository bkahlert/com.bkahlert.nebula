package com.bkahlert.nebula.gallery.demoExplorer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;


import com.bkahlert.devel.nebula.utils.ViewerUtils;
import com.bkahlert.nebula.gallery.demoSuits.MainDemo;
import com.bkahlert.nebula.gallery.util.deprecated.LayoutUtils;
import com.bkahlert.nebula.gallery.util.deprecated.TreeLabelProvider;
import com.bkahlert.nebula.gallery.util.deprecated.ViewerComposite;

public class DemoExplorer extends ViewerComposite {

	public DemoExplorer(Composite parent, int style) {
		super(parent, style);

		super.setLayout(LayoutUtils.createGridLayout());
		this.viewer.getControl()
				.setLayoutData(LayoutUtils.createFillGridData());
		this.viewer.setInput(MainDemo.class);
		ViewerUtils.expandToLevel(this.viewer, 2);
	}

	@Override
	protected void createViewer(int style) {
		this.viewer = new TreeViewer(new Tree(this, style));
	}

	@Override
	protected void configureViewer() {
		this.viewer.setContentProvider(new DemoContentProvider());
		this.viewer.setLabelProvider(new TreeLabelProvider());
		this.viewer.setUseHashlookup(true);
	}

	@Override
	public void setLayout(Layout layout) {
		// ignore
	}

	public DemoElement getSelectedDemoElement() {
		DemoElement demoElement = (DemoElement) ((IStructuredSelection) this.viewer
				.getSelection()).getFirstElement();
		if (demoElement == null) {
			demoElement = new DemoElement(MainDemo.class);
		}
		return demoElement;
	}
}
