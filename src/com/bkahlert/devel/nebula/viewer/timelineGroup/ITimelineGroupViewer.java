package com.bkahlert.devel.nebula.viewer.timelineGroup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

/**
 * Instances of this class can be used to extend {@link ITimeline}s with MVC
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineGroupViewer extends ITimelineViewer {
	/**
	 * Returns the viewer's underlying {@link ITimelineGroup}.
	 * <p>
	 * May be safely casted to ITimelineGroup.
	 * 
	 * @return
	 */
	@Override
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
	@Override
	public void setInput(Object input);

	/**
	 * Updates the UI element associated with the given object.
	 * 
	 * @param element
	 */
	public void update(Object object);

	/**
	 * Refreshes the user interface based on a freshly reloaded model.
	 */
	@Override
	public void refresh(IProgressMonitor monitor);
}
