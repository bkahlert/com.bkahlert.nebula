package com.bkahlert.devel.nebula.widgets.timeline;

import org.eclipse.jface.viewers.ISelectionProvider;

import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;

/**
 * Extends the {@link Timeline} by tracking functionality.
 * <p>
 * This implementation further implement the an {@link ISelectionProvider}.
 * 
 * @author bkahlert
 * 
 */
public interface ISelectionTimeline extends ITimeline, ISelectionProvider {

}