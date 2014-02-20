package com.bkahlert.nebula.viewer;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;


import com.bkahlert.devel.rcp.selectionUtils.ArrayUtils;
import com.bkahlert.nebula.gallery.util.deprecated.LayoutUtils;
import com.bkahlert.nebula.gallery.util.deprecated.ViewerComposite;

/**
 * This {@link Composite} displays all {@link IProject}s within the
 * {@link IWorkbench}.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE and those supported by {@link StructuredViewer}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author bkahlert
 * 
 */
public class ProjectDisplayComposite extends ViewerComposite {
	public ProjectDisplayComposite(Composite parent, int style) {
		super(parent, style);

		super.setLayout(LayoutUtils.createGridLayout());
		this.viewer.getControl()
				.setLayoutData(LayoutUtils.createFillGridData());
		this.viewer.setInput(ResourcesPlugin.getWorkspace());
	}

	/**
	 * Creates the viewer
	 * 
	 * @param style
	 */
	@Override
	protected void createViewer(int style) {
		this.viewer = new TableViewer(new Table(this, style));
	}

	/**
	 * Configures the viewer
	 */
	@Override
	protected void configureViewer() {
		this.viewer
				.setContentProvider(new ProjectOnlyWorkbenchContentProvider());
		this.viewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		this.viewer.setUseHashlookup(true);
	}

	/**
	 * Returns the displayed {@link IProject}s.
	 * 
	 * @return
	 */
	public List<IProject> getProjects() {
		WorkbenchContentProvider contentProvider = (WorkbenchContentProvider) this.viewer
				.getContentProvider();
		Object[] objects = contentProvider
				.getElements(((TableViewer) this.viewer).getInput());
		return ArrayUtils.getAdaptableObjects(objects, IProject.class);
	}

	@Override
	public void setLayout(Layout layout) {
		// ignore
	}
}
