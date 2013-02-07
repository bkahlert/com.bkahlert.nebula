package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
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

	protected static String getFileUrl(Class<?> clazz, String clazzRelativePath)
			throws IOException {
		URL timelineUrl = FileLocator.toFileURL(clazz
				.getResource(clazzRelativePath));
		String timelineUrlString = timelineUrl.toString().replace("file:",
				"file://");
		return timelineUrlString;
	}

	protected static List<ITimelineEvent> getSortedEvents(ITimelineInput input) {
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

	private Logger logger = Logger.getLogger(BaseTimeline.class);

	private boolean completedLoading = false;
	private List<String> enqueuedJs = new ArrayList<String>();

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

		this.getBrowser().addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				completedLoading = true;
				for (Iterator<String> iterator = enqueuedJs.iterator(); iterator
						.hasNext();) {
					String js = iterator.next();
					iterator.remove();
					if (!BaseTimeline.this.getBrowser().execute(js)) {
						logger.error("Error occured while running JavaScript in browser: "
								+ js);
					}
				}
			}
		});

		try {
			String timelineUrlString = getFileUrl(BaseTimeline.class,
					"../html/timeline.html");
			this.getBrowser().setUrl(timelineUrlString + "?internal=true");
		} catch (IOException e) {
			logger.error("Could not open timeline html", e);
		}
	}

	@Override
	public boolean runJs(String js) {
		boolean success = this.getBrowser().execute(js);
		if (!success) {
			logger.error("Error occured while running JavaScript in browser: "
					+ js);
		}
		return success;
	}

	@Override
	public void enqueueJs(String js) {
		if (completedLoading) {
			runJs(js);
		} else {
			enqueuedJs.add(js);
		}
	}

	@Override
	public void injectCssFile(String path) {
		String js = "if(document.createStyleSheet){document.createStyleSheet(\""
				+ path
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ path + "\\\" type=\\\"text/css\\\" />\"))}";
		enqueueJs(js);
	}

	/**
	 * Display the given JSON string on the {@link IBaseTimeline}. The format is
	 * quite complex.<br>
	 * It is therefore preferable to use
	 * {@link #show(ITimelineInput, IProgressMonitor)}.
	 * <p>
	 * May be called from whatever thread.
	 * 
	 * @param jsonTimeline
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li> <li>ERROR_WIDGET_DISPOSED when the widget has
	 *                been disposed</li>
	 *                </ul>
	 */
	private void show(final String jsonTimeline) {
		System.err.println(jsonTimeline);
		final String escapedJson = TimelineJsonGenerator.escape(jsonTimeline);
		final Runnable showRunnable = new Runnable() {
			@Override
			public void run() {
				BaseTimeline.this
						.enqueueJs("com.bkahlert.devel.nebula.timeline.loadJSON('"
								+ escapedJson + "');");
				BaseTimeline.this.layout();
			}
		};
		if (Display.getCurrent() == Display.getDefault()) {
			showRunnable.run();
		} else {
			Display.getDefault().asyncExec(showRunnable);
		}
	}

	/**
	 * Displays the given input
	 * <p>
	 * May be called from whatever Thread.
	 */
	public void show(ITimelineInput input, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1 + 10);

		this.sortedEvents = getSortedEvents(input);
		subMonitor.worked(1);

		String json = TimelineJsonGenerator.toJson(input, false,
				subMonitor.newChild(10));

		this.show(json);
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
	public void setMaxVisibleDate(Calendar calendar) {
		if (!isDisposed()) {
			this.getBrowser().execute(
					"com.bkahlert.devel.nebula.timeline.setMaxVisibleDate('"
							+ calendar + "');");
		}
	}

	@Override
	public void applyDecorators(String jsonDecorators) {
		if (!isDisposed()) {
			this.getBrowser().execute(
					"com.bkahlert.devel.nebula.timeline.applyDecorators('"
							+ TimelineJsonGenerator.escape(jsonDecorators)
							+ "');");
		}
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
