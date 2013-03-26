package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class TimelineDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite composite) {
		ITimeline timeline = new Timeline(composite, SWT.BORDER);
		ITimelineInput input = null; // TODO fill with ITimelineInput
		timeline.show(input, 500, 500, null);
	}

}
