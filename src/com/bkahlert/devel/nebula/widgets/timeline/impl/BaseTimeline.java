package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
		if (input == null)
			return null;
		List<ITimelineEvent> events = new LinkedList<ITimelineEvent>();
		for (ITimelineBand band : input.getBands())
			for (ITimelineEvent event : band.getEvents())
				events.add(event);
		Collections.sort(events, new Comparator<ITimelineEvent>() {
			@Override
			public int compare(ITimelineEvent o1, ITimelineEvent o2) {
				if (o1 == null)
					return -1;
				Calendar t1 = o1.getStart();
				if (t1 == null)
					t1 = o1.getEnd();

				Calendar t2 = o2.getStart();
				if (t2 == null)
					t2 = o2.getEnd();

				if (t1 == null && t2 == null)
					return 0;
				if (t1 != null && t2 == null)
					return +1;
				if (t1 == null && t2 != null)
					return -1;
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

		/*
		 * Deactivate browser's native context/popup menu. Doing so allows the
		 * definition of menus in an inheriting composite via setMenu.
		 */
		this.getBrowser().addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
			}
		});
	}

	@Override
	public String getStartUrl() {
		try {
			String timelineUrlString = getFileUrl(BaseTimeline.class,
					"../html/timeline.html");
			return timelineUrlString + "?internal=true";
		} catch (IOException e) {
			LOGGER.error("Could not open timeline html", e);
		}
		return null;
	}

	/**
	 * Display the given JSON string on the {@link IBaseTimeline}. The format is
	 * quite complex. It is therefore preferable to use
	 * {@link #show(ITimelineInput, IProgressMonitor)} or
	 * {@link #show(ITimelineInput, int, int, IProgressMonitor)}.
	 * <p>
	 * May be called from whatever thread.
	 * 
	 * @param jsonTimeline
	 */
	private void show(final String jsonTimeline,
			final int startAnimationDuration, final int endAnimationDuration) {
		System.err.println(jsonTimeline);
		final String escapedJson = TimelineJsonGenerator.enquote(jsonTimeline);

		final String js;
		if (startAnimationDuration <= 0 || endAnimationDuration <= 0) {
			js = "com.bkahlert.devel.nebula.timeline.loadJSON(" + escapedJson
					+ ");";
		} else {
			js = "com.bkahlert.devel.nebula.timeline.loadJSONAnimated("
					+ escapedJson + ", null, " + startAnimationDuration + ", "
					+ endAnimationDuration + ");";
		}

		ExecutorUtil.asyncExec(new Runnable() {
			@Override
			public void run() {
				BaseTimeline.this.enqueueJs(js);
				// TODO layout als callback; evtl. Browser noch nicht geladen
				BaseTimeline.this.layout();
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

		String json = TimelineJsonGenerator.toJson(input, false,
				subMonitor.newChild(10));

		this.show(json, startAnimationDuration, endAnimationDuration);
	}

	@Override
	public void setMinVisibleDate(Calendar calendar) {
		if (!isDisposed()) {
			this.getBrowser().execute(
					"com.bkahlert.devel.nebula.timeline.setMinVisibleDate('"
							+ calendar + "');");
		}
	}

	@Override
	public void setCenterVisibleDate(Calendar calendar) {
		if (!isDisposed()) {
			this.getBrowser().execute(
					"com.bkahlert.devel.nebula.timeline.setCenterVisibleDate('"
							+ CalendarUtils.toISO8601(calendar) + "');");
		}
	}

	@Override
	public Calendar getCenterVisibleDate() {
		if (!isDisposed()) {
			String centerVisibleDate = (String) this
					.getBrowser()
					.evaluate(
							"return com.bkahlert.devel.nebula.timeline.getCenterVisibleDate();");
			if (centerVisibleDate != null)
				return CalendarUtils.fromISO8601(centerVisibleDate);
		}
		return null;
	}

	@Override
	public void setMaxVisibleDate(Calendar calendar) {
		if (!isDisposed()) {
			this.getBrowser().execute(
					"com.bkahlert.devel.nebula.timeline.setMaxVisibleDate('"
							+ calendar + "');");
		}
	}

	@Override
	public void setZoomIndex(final int index) {
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!isDisposed()) {
					getBrowser().execute(
							"com.bkahlert.devel.nebula.timeline.setZoomIndex("
									+ index + ");");
				}
			}
		});
	}

	@Override
	public Integer getZoomIndex() {
		if (!isDisposed()) {
			Double zoomIndex = (Double) this
					.getBrowser()
					.evaluate(
							"return com.bkahlert.devel.nebula.timeline.getZoomIndex();");
			return zoomIndex != null ? (int) Math.round(zoomIndex) : null;
		}
		return null;
	}

	@Override
	public void setDecorators(IDecorator[] decorators) {
		this.decorators = decorators;

		final String decoratorJSON = TimelineJsonGenerator.toJson(decorators,
				false);

		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!isDisposed()) {
					getBrowser().execute(
							"com.bkahlert.devel.nebula.timeline.setDecorators("
									+ TimelineJsonGenerator
											.enquote(decoratorJSON) + ");");
				}
			}
		});
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
		if (getSortedEvents() != null) {
			int index = getSortedEvents().indexOf(event);
			return index;
		}
		return -1;
	}

	public ITimelineEvent getSuccessor(ITimelineEvent event) {
		int index = getIndex(event);
		if (index >= 0 && index < getSortedEvents().size() - 1) {
			return getSortedEvents().get(index + 1);
		}
		return null;
	}

	public ITimelineEvent getPredecessor(ITimelineEvent event) {
		int index = getIndex(event);
		if (index > 0) {
			return getSortedEvents().get(index - 1);
		}
		return null;
	}

}
