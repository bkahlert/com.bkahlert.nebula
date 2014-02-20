package com.bkahlert.nebula.viewer;

/**
 * Listener for {@link ProjectSelectionComposite} events.
 */
public interface ProjectSelectionListener extends BaseProjectSelectionListener {

	/**
	 * Gets called whenever the
	 * {@link ProjectSelectionComposite#filterClosedProjects} option changed.
	 * 
	 * @param event
	 */
	public void filterClosedProjectsChanged(
			FilterClosedProjectsChangedEvent event);

}