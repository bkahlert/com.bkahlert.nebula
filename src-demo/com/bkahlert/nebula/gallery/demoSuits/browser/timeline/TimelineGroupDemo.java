package com.bkahlert.nebula.gallery.demoSuits.browser.timeline;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.ITimelineFactory;
import com.bkahlert.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.nebula.widgets.timeline.impl.Options;
import com.bkahlert.nebula.widgets.timeline.impl.Timeline;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

@Demo
public class TimelineGroupDemo extends AbstractDemo {

	@SuppressWarnings("serial")
	public ITimelineInput getInput() {
		final Calendar start1 = Calendar.getInstance();
		final Calendar end1 = Calendar.getInstance();
		end1.add(Calendar.HOUR, 1);

		final Calendar start2 = Calendar.getInstance();
		final Calendar end2 = Calendar.getInstance();
		start2.add(Calendar.HOUR, 1);
		end2.add(Calendar.HOUR, 2);
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
											"Title 1",
											"Tooltip 1",
											new URI(
													"http://aux4.iconpedia.net/uploads/868888537436999746.png"),
											new URI(
													"http://www.w3.org/html/logo/downloads/HTML5_Badge_512.png"),
											start1, end1, new RGB[] { new RGB(
													0.259, 0.706, 0.902) },
											true, null, "Payload #1"));

									this.add(new TimelineEvent(
											"Title 2",
											"Tooltip 2",
											new URI(
													"http://aux4.iconpedia.net/uploads/868888537436999746.png"),
											new URI(
													"http://www.w3.org/html/logo/downloads/HTML5_Badge_512.png"),
											start2, end2, new RGB[] { new RGB(
													0.559, 0.706, 0.902) },
											true, null, "Payload #2"));
								}
							}));
				}
			});
		} catch (URISyntaxException e) {
			log(e.getMessage());
		}
		return input;
	}

	@Override
	public void createDemo(Composite composite) {
		TimelineGroup<ITimeline, URI> timelineGroup = new TimelineGroup<ITimeline, URI>(
				composite, SWT.NONE, new ITimelineFactory<ITimeline>() {
					@Override
					public ITimeline createTimeline(Composite parent, int style) {
						return new Timeline(parent, style);
					}
				});

		try {
			Map<URI, ITimelineInput> inputs = new HashMap<URI, ITimelineInput>();
			inputs.put(new URI("a"), this.getInput());
			inputs.put(new URI("b"), this.getInput());
			timelineGroup.show(inputs, null, new Callable<String>() {
				@Override
				public String call() throws Exception {
					log(TimelineGroup.class + " successfully loaded");
					return null;
				}
			});
		} catch (Exception e) {
			log(e.getMessage());
		}

		timelineGroup.addTimelineListener(new ITimelineListener() {

			@Override
			public void rightClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("right clicked: " + event);
			}

			@Override
			public void resizing(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("resizing: " + event);
			}

			@Override
			public void resized(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("resized: " + event);
			}

			@Override
			public void resizeStarted(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("resize started: " + event);
			}

			@Override
			public void middleClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("middle clicked: " + event);
			}

			@Override
			public void hoveredOut(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("hovered out: " + event);
			}

			@Override
			public void hoveredIn(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("hovered in: " + event);
			}

			@Override
			public void doubleClicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("double clicked: " + event);
			}

			@Override
			public void clicked(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("left clicked: " + event);
			}

			@Override
			public void selected(
					com.bkahlert.nebula.widgets.timeline.TimelineEvent event) {
				log("selected: " + event);
			}
		});
	}

}
