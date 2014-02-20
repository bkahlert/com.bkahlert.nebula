package com.bkahlert.nebula.viewer;

import org.eclipse.core.resources.IProject;

public class ProjectSelectionChangedEvent {
	private IProject project;
	private boolean isSelected;

	/**
	 * @param project
	 *            {@link IProject} who's selection changed
	 * @param isSelected
	 *            new selection state
	 */
	public ProjectSelectionChangedEvent(IProject project, boolean isSelected) {
		super();
		this.project = project;
		this.isSelected = isSelected;
	}

	public IProject getProject() {
		return this.project;
	}

	public boolean isSelected() {
		return this.isSelected;
	}

}
