package com.bkahlert.nebula.gallery.demoSuits.browser.timeline;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.impl.Options;
import com.bkahlert.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;

@Demo
public class TimelineDemo extends AbstractDemo {

	@SuppressWarnings("serial")
	@Override
	public void createDemo(Composite composite) {
		ITimeline timeline = new Timeline(composite, SWT.BORDER);

		final Calendar start = Calendar.getInstance();
		final Calendar end = Calendar.getInstance();
		end.add(Calendar.HOUR, 1);
		ITimelineInput input = null;
		try {
			input = new TimelineInput(new Options() {
				{
					this.setTimeZone(1.0f);
				}
			}, new ArrayList<ITimelineBand>() {
				{
					this.add(new TimelineBand(new Options(),
							new ArrayList<ITimelineEvent>() {
								{
									this.add(new TimelineEvent(
											"Title",
											"Tooltip",
											new URI(
													"http://aux4.iconpedia.net/uploads/868888537436999746.png"),
											new URI(
													"http://www.w3.org/html/logo/downloads/HTML5_Badge_512.png"),
											start, end, new RGB[] { new RGB(
													0.259, 0.706, 0.902) },
											true, null, "Event Number #1"));
								}
							}));
				}
			});
		} catch (URISyntaxException e) {
			log(e.getMessage());
		}
		timeline.show(input, 500, 500, null);
	}

}
