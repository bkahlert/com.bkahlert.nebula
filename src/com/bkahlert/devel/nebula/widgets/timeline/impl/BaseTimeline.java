package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.utils.IConverter;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.nebula.browser.BrowserUtils;

/**
 * This is a very basic timeline implementation that does not handle any
 * timeline events such as clicking or resizing.
 * 
 * @author bkahlert
 * 
 */
public class BaseTimeline extends BrowserComposite implements IBaseTimeline {

	protected static List<ITimelineEvent> getSortedEvents(ITimelineInput input) {
		if (input == null) {
			return null;
		}
		List<ITimelineEvent> events = new LinkedList<ITimelineEvent>();
		for (ITimelineBand band : input.getBands()) {
			for (ITimelineEvent event : band.getEvents()) {
				events.add(event);
			}
		}
		Collections.sort(events, new Comparator<ITimelineEvent>() {
			@Override
			public int compare(ITimelineEvent o1, ITimelineEvent o2) {
				if (o1 == null) {
					return -1;
				}
				Calendar t1 = o1.getStart();
				if (t1 == null) {
					t1 = o1.getEnd();
				}

				Calendar t2 = o2.getStart();
				if (t2 == null) {
					t2 = o2.getEnd();
				}

				if (t1 == null && t2 == null) {
					return 0;
				}
				if (t1 != null && t2 == null) {
					return +1;
				}
				if (t1 == null && t2 != null) {
					return -1;
				}
				return t1.compareTo(t2);
			}
		});
		return events;
	}

	@SuppressWarnings("unused")
	private static Logger LOGGER = Logger.getLogger(BaseTimeline.class);

	private IDecorator[] permanentDecorators = null;
	private IDecorator[] decorators = null;
	private List<ITimelineEvent> sortedEvents = null;

	public BaseTimeline(Composite parent, int style) {
		super(parent, style);
		this.deactivateNativeMenu();
		this.open(
				BrowserUtils.getFileUrl(BaseTimeline.class, "../html/timeline.html",
						"?internal=true"),
				30000,
				"return typeof jQuery != \"undefined\" && jQuery('html').hasClass('timeline-ready')");
	}

	/**
	 * Display the given JSON string on the {@link IBaseTimeline}. The format is
	 * quite complex. It is therefore preferable to use
	 * {@link #show(ITimelineInput, IProgressMonitor)} or
	 * {@link #show(ITimelineInput, int, int, IProgressMonitor)}.
	 * <p>
	 * May be called from whatever thread.
	 * 
	 * @param json
	 * @throws Exception
	 */
	private void show(final File json, final int startAnimationDuration,
			final int endAnimationDuration) throws Exception {
		// System.err.println(jsonTimeline);
		// final String escapedJson = TimelineJsonGenerator.enquote(json);
		final String js;
		if (startAnimationDuration <= 0 || endAnimationDuration <= 0) {
			js = "com.bkahlert.devel.nebula.timeline.loadJSON(\"file://"
					+ json.getAbsolutePath() + "\");";
		} else {
			js = "com.bkahlert.devel.nebula.timeline.loadJSONAnimated(\"file://"
					+ json.getAbsolutePath()
					+ "\", null, "
					+ startAnimationDuration
					+ ", "
					+ endAnimationDuration
					+ ");";
		}
		this.run(js).get();
		ExecUtils.syncExec(new Runnable() {
			@Override
			public void run() {
				BaseTimeline.this.layout();
			}
		});
	}

	@Override
	public Future<Void> show(ITimelineInput input, IProgressMonitor monitor) {
		return this.show(input, -1, -1, monitor);
	}

	@Override
	public Future<Void> show(final ITimelineInput input,
			final int startAnimationDuration, final int endAnimationDuration,
			final IProgressMonitor monitor) {

		if (input != null && input.getOptions() != null) {
			this.permanentDecorators = input.getOptions()
					.getPermanentDecorators();
			this.decorators = input.getOptions().getDecorators();
		} else {
			this.permanentDecorators = null;
			this.decorators = null;
		}

		return ExecUtils.nonUIAsyncExec(BaseTimeline.class, "Showing timeline",
				new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						SubMonitor subMonitor = SubMonitor.convert(monitor,
								1 + 10);

						BaseTimeline.this.sortedEvents = getSortedEvents(input);
						subMonitor.worked(1);

						File json = TimelineJsonGenerator.toJson(input, false,
								subMonitor.newChild(10));
						BaseTimeline.this.show(json, startAnimationDuration,
								endAnimationDuration);
						return null;
					}
				});
	}

	@Override
	public void setMinVisibleDate(Calendar calendar) {
		this.run("com.bkahlert.devel.nebula.timeline.setMinVisibleDate('"
				+ calendar + "');");
	}

	@Override
	public void setCenterVisibleDate(Calendar calendar) {
		this.run("com.bkahlert.devel.nebula.timeline.setCenterVisibleDate('"
				+ CalendarUtils.toISO8601(calendar) + "');");
	}

	@Override
	public Future<Calendar> getCenterVisibleDate() {
		return this
				.run("return com.bkahlert.devel.nebula.timeline.getCenterVisibleDate();",
						new IConverter<Object, Calendar>() {
							@Override
							public Calendar convert(Object returnValue) {
								String centerVisibleDate = (String) returnValue;
								if (centerVisibleDate != null) {
									return CalendarUtils
											.fromISO8601(centerVisibleDate);
								} else {
									return null;
								}
							}
						});
	}

	@Override
	public void setMaxVisibleDate(Calendar calendar) {
		this.run("com.bkahlert.devel.nebula.timeline.setMaxVisibleDate('"
				+ calendar + "');");
	}

	@Override
	public void setZoomIndex(final int index) {
		this.run("com.bkahlert.devel.nebula.timeline.setZoomIndex(" + index
				+ ");");
	}

	@Override
	public Future<Integer> getZoomIndex() {
		return this.run(
				"return com.bkahlert.devel.nebula.timeline.getZoomIndex();",
				new IConverter<Object, Integer>() {
					@Override
					public Integer convert(Object returnValue) {
						Double zoomIndex = (Double) returnValue;
						return zoomIndex != null ? (int) Math.round(zoomIndex)
								: null;
					}
				});
	}

	@Override
	public IDecorator[] getPermanentDecorators() {
		return this.permanentDecorators;
	}

	@Override
	public void setDecorators(IDecorator[] decorators) {
		this.decorators = decorators;

		final String decoratorJSON = TimelineJsonGenerator.toJson(decorators,
				false);

		this.run("com.bkahlert.devel.nebula.timeline.setDecorators("
				+ TimelineJsonGenerator.enquote(decoratorJSON) + ");");
	}

	@Override
	public IDecorator[] getDecorators() {
		return this.decorators;
	}

	@Override
	public boolean setFocus() {
		return true;
	}

	protected List<ITimelineEvent> getSortedEvents() {
		return this.sortedEvents;
	}

	private int getIndex(ITimelineEvent event) {
		if (this.getSortedEvents() != null) {
			int index = this.getSortedEvents().indexOf(event);
			return index;
		}
		return -1;
	}

	@Override
	public ITimelineEvent getSuccessor(ITimelineEvent event) {
		int index = this.getIndex(event);
		if (index >= 0 && index < this.getSortedEvents().size() - 1) {
			return this.getSortedEvents().get(index + 1);
		}
		return null;
	}

	@Override
	public ITimelineEvent getPredecessor(ITimelineEvent event) {
		int index = this.getIndex(event);
		if (index > 0) {
			return this.getSortedEvents().get(index - 1);
		}
		return null;
	}

}
