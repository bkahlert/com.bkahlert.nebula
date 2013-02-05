package com.bkahlert.devel.nebula.viewer.timeline;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

public interface ITimelineViewer {
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
