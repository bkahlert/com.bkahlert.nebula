package com.bkahlert.devel.nebula.viewer.timeline;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

/**
 * Instances of this class can be used to extend {@link ITimeline}s with MVC
 * functionality.
 * 
 * @author bkahlert
 * 
 */
public interface ITimelineViewer extends IInputSelectionProvider {
	/**
	 * Separator that is used to encode the IDs of {@link ITimelineEvent} in
	 * their class names.
	 */
	public static final String CSS_IDENTIFIER_SEPARATOR = "_-_";

	/**
	 * Returns the viewer's underlying {@link ITimeline}.
	 * <p>
	 * May be safely casted to {@link ITimeline}.
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

	public ITimelineLabelProvider getTimelineLabelProvider();

	public void setTimelineLabelProvider(
			ITimelineLabelProvider timelineLabelProvider);

	public void refresh(IProgressMonitor monitor);
}
