package com.bkahlert.nebula.widgets.timeline.model;

import com.bkahlert.nebula.widgets.timeline.impl.HotZone;

/**
 * A {@link HotZone} denotes a time span that should be magnified when rendered.
 * 
 * TODO Allow to define the magnification factor.
 * 
 * @author bkahlert
 * 
 */
public interface IHotZone {

	public String getStart();

	public String getEnd();

}