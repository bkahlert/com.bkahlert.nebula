package com.bkahlert.devel.nebula.viewer.timelineGroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

/**
 * Instances of this class can be used to extend {@link ITimeline}s with MVC
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineGroupViewer extends IInputSelectionProvider {
	/**
	 * Returns the viewer's underlying {@link ITimeline}.
	 * <p>
	 * May be safely casted to TIMELINE.
	 * 
	 * @return
	 */
	public Control getControl();

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
	public void setInput(Object input);

	public void refresh(IProgressMonitor monitor);
}
