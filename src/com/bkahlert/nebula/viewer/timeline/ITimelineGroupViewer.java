package com.bkahlert.nebula.viewer.timeline;

import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IInputSelectionProvider;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * Instances of this class can be used to extend {@link ITimeline}s with MVC
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
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
	 * 
	 * @return true if refresh finished successfully
	 */
	public Future<Void> refresh(IProgressMonitor monitor);
}
