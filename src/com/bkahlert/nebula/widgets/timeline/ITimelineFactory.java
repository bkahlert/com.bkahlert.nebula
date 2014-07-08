package com.bkahlert.nebula.widgets.timeline;

import org.eclipse.swt.widgets.Composite;

/**
 * Instances of this class create {@code <TIMELINE>}s.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINE>
 */
public interface ITimelineFactory<TIMELINE extends IBaseTimeline> {
	/**
	 * Creates a {@code <TIMELINE>}.
	 * 
	 * @param parent
	 * @param style
	 * @return
	 */
	public TIMELINE createTimeline(Composite parent, int style);
}
