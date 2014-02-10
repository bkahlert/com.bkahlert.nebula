package com.bkahlert.nebula.viewer.timeline.provider.atomic;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;

/**
 * Provides the {@link ITimeline} with contents.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineContentProvider<INPUT> {
	public <TIMELINE extends IBaseTimeline> void inputChanged(
			ITimelineGroupViewer<TIMELINE, INPUT> timelineGroupViewer,
			INPUT oldInput, INPUT newInput);

	/**
	 * Returns true if this {@link ITimelineContentProvider} considers the key
	 * valid.
	 * <p>
	 * Typically this means that it has access to a resource identified by this
	 * key.
	 * 
	 * @param input
	 * @return
	 */
	public boolean isValid(INPUT input);

	public Object[] getBands(IProgressMonitor monitor);

	public Object[] getEvents(Object band, IProgressMonitor monitor);

}
