package com.bkahlert.devel.nebula.viewer.timeline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IInputSelectionProvider;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;

/**
 * Instances of this class can be used to extend {@link ITimeline}s with MVC
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE>, TIMELINE extends ITimeline, INPUT>
		extends IInputSelectionProvider {

	/**
	 * Returns the viewer's underlying {@link ITimelineGroup}.
	 * <p>
	 * May be safely casted to ITimelineGroup.
	 * 
	 * @return
	 */
	public TIMELINEGROUP getControl();

	/**
	 * Sets the {@link ITimeline}'s input.
	 * <p>
	 * The input is passed to methods like
	 * {@link ITimelineContentProvider#getBands(Object)} and
	 * {@link ITimelineEventLabelProvider#getIcon(Object)}.
	 * 
	 * @param input
	 * @param monitor
	 */
	public void setInput(INPUT input);

	/**
	 * Updates the UI element associated with the given object.
	 * 
	 * @param element
	 */
	public void update(Object object);

	/**
	 * Refreshes the user interface based on a freshly reloaded model.
	 */
	public void refresh(IProgressMonitor monitor);
}
