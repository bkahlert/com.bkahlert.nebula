package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

/**
 * This is a very basic timeline implementation.
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

	private static Logger LOGGER = Logger.getLogger(BaseTimeline.class);

	private IDecorator[] decorators = null;
	private List<ITimelineEvent> sortedEvents = null;

	public BaseTimeline(Composite parent, int style) {
		super(parent, style);
		this.deactivateNativeMenu();
		this.open(
				getFileUrl(BaseTimeline.class, "../html/timeline.html",
						"?internal=true"), 10000);
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
	 */
	private void show(final File json, final int startAnimationDuration,
			final int endAnimationDuration) {
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
		final Future<Object> rt = this.run(js);
		ExecutorUtil.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					rt.get();
				} catch (Exception e) {
				}
				ExecutorUtil.asyncExec(new Runnable() {
					@Override
					public void run() {
						BaseTimeline.this.layout();
					}
				});
			}
		});
	}

	@Override
	public void show(ITimelineInput input, IProgressMonitor monitor) {
		this.show(input, -1, -1, monitor);
	}

	@Override
	public void show(ITimelineInput input, int startAnimationDuration,
			int endAnimationDuration, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1 + 10);

		this.sortedEvents = getSortedEvents(input);
		subMonitor.worked(1);

		try {
			File json = TimelineJsonGenerator.toJson(input, false,
					subMonitor.newChild(10));
			this.show(json, startAnimationDuration, endAnimationDuration);
		} catch (IOException e) {
			LOGGER.error(
					"Error serializing JSON for "
							+ BaseTimeline.class.getSimpleName(), e);
		}

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
						new IConverter<Calendar>() {
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
				new IConverter<Integer>() {
					@Override
					public Integer convert(Object returnValue) {
						Double zoomIndex = (Double) returnValue;
						return zoomIndex != null ? (int) Math.round(zoomIndex)
								: null;
					}
				});
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
