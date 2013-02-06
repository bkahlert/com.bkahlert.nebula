package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Date;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

public class TimelineEvent extends TypedEvent {

	private static final long serialVersionUID = -187925793091438074L;

	/**
	 * the underlying {@link ITimelineEvent} that represented the
	 * {@link #getSource()}
	 */
	public ITimelineEvent timelineEvent;

	public TimelineEvent(Display display, ITimeline timeline,
			ITimelineEvent timelineEvent) {
		super(timelineEvent.getPayload());
		this.display = display;
		this.time = (int) (new Date().getTime() & 0xFFFFFFFFL);
		this.widget = (Widget) timeline;
		this.timelineEvent = timelineEvent;
	}
}
