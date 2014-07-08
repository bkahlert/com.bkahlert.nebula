package com.bkahlert.nebula.widgets.timelinegroup;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;

public interface IBaseTimelineGroup<TIMELINE extends IBaseTimeline, INPUT> {

	public abstract <T> Future<T> show(Map<INPUT, ITimelineInput> inputs,
			IProgressMonitor monitor, Callable<T> success);

	public abstract TIMELINE createTimeline();

	public abstract Set<INPUT> getTimelineKeys();

	/**
	 * Returns the {@code TIMELINE} that is associated with the given key.
	 * 
	 * @UI must be called from the UI thread
	 * @param key
	 * @return
	 */
	public abstract TIMELINE getTimeline(INPUT key);

}