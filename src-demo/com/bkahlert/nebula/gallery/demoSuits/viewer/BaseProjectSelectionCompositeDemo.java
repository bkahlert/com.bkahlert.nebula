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
import com.bkahlert.nebula.viewer.BaseProjectSelectionComposite;

@Demo(title = "This demo show a BaseProjectSelectionComposite that reflects the currently selected projects in the workbench.")
public class BaseProjectSelectionCompositeDemo extends AbstractDemo {
	protected BaseProjectSelectionComposite baseProjectSelectionComposite;

	protected ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			BaseProjectSelectionCompositeDemo.this.baseProjectSelectionComposite
					.setSelectedProjects(SelectionRetrieverFactory
							.getSelectionRetriever(IProject.class)
							.getOverallSelection());
		}
	};

	@Override
	public void createDemo(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		this.baseProjectSelectionComposite = new BaseProjectSelectionComposite(
				parent, SWT.BORDER);
		this.baseProjectSelectionComposite.setLayoutData(new GridData(SWT.FILL,
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
