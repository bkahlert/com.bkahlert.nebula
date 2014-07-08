package com.bkahlert.nebula.viewer;

/**
 * Listener for {@link BaseProjectSelectionComposite} events.
 */
public interface BaseProjectSelectionListener {

	/**
	 * Gets called whenever a {@link IProject} selection changed.
	 * 
	 * @param event
	 */
	public void projectSelectionChanged(ProjectSelectionChangedEvent event);

}