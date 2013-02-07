package com.bkahlert.devel.nebula.widgets.timelineGroup;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.devel.nebula.widgets.IWidget;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

public interface ITimelineGroup<TIMELINE extends IBaseTimeline> extends IWidget {
	/**
	 * Displays the given {@link ITimelineGroupInput}.
	 * <p>
	 * For this every {@link ITimelineInput} gets one timeline.
	 * 
	 * @param inputs
	 * @param progressMonitor
	 * @param success
	 * @return
	 */
	public <T> Future<T> show(Set<ITimelineInput> inputs,
			IProgressMonitor monitor, Callable<T> success);

	public void addTimelineListener(ITimelineListener timelineListener);

	public void removeTimelineListener(ITimelineListener timelineListener);
}
