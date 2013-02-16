package com.bkahlert.devel.nebula.viewer.timeline.provider.atomic;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;

/**
 * Provides the {@link ITimeline} with contents.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineContentProvider {
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput);

	/**
	 * Returns true if this {@link ITimelineBandProvider} considers the key
	 * valid.
	 * <p>
	 * Typically this means that it has access to a resource identified by this
	 * key.
	 * 
	 * @param key
	 * @return
	 */
	public boolean isValid(Object key);

	public Object[] getBands(IProgressMonitor monitor);

	public Object[] getEvents(Object band, IProgressMonitor monitor);
}
