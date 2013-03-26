package com.bkahlert.nebula.gallery.demoSuits.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;


import com.bkahlert.devel.nebula.widgets.viewer.ProjectDisplayComposite;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class ProjectDisplayCompositeDemo extends AbstractDemo {
	protected ProjectDisplayComposite projectDisplayComposite;

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new FillLayout());

		this.projectDisplayComposite = new ProjectDisplayComposite(parent,
				SWT.BORDER);
	}
}
