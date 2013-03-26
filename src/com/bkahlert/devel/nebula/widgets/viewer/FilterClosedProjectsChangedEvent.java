package com.bkahlert.devel.nebula.widgets.viewer;

public class FilterClosedProjectsChangedEvent {
	private boolean filterClosedProjects;

	/**
	 * @param filterClosedProjects
	 */
	public FilterClosedProjectsChangedEvent(boolean filterClosedProjects) {
		super();
		this.filterClosedProjects = filterClosedProjects;
	}

	public boolean isFilterClosedProjects() {
		return this.filterClosedProjects;
	}

}
