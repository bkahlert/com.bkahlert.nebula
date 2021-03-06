package com.bkahlert.nebula.gallery.demoSuits.viewer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.utils.selection.retriever.SelectionRetrieverFactory;
import com.bkahlert.nebula.viewer.ProjectSelectionComposite;

@Demo(title = "This demo shows a Composite that reflects the currently selected projects in the workbench.")
public class ProjectSelectionCompositeDemo extends AbstractDemo {
	protected ProjectSelectionComposite projectSelectionComposite;

	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			ProjectSelectionCompositeDemo.this.projectSelectionComposite
					.setSelectedProjects(SelectionRetrieverFactory
							.getSelectionRetriever(IProject.class)
							.getOverallSelection());
		}
	};

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		this.projectSelectionComposite = new ProjectSelectionComposite(parent,
				SWT.BORDER, true);
		this.projectSelectionComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, true));
		SelectionUtils.getSelectionService().addSelectionListener(
				this.selectionListener);
	}

	@Override
	public void dispose() {
		SelectionUtils.getSelectionService().removeSelectionListener(
				this.selectionListener);
		super.dispose();
	}
}
