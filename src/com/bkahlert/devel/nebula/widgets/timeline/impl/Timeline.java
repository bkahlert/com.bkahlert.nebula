package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;

public class Timeline extends BrowserComposite implements ITimeline {

	public static String getFileUrl(Class<?> clazz, String clazzRelativePath)
			throws IOException {
		URL timelineUrl = FileLocator.toFileURL(clazz
				.getResource(clazzRelativePath));
		String timelineUrlString = timelineUrl.toString().replace("file:",
				"file://");
		return timelineUrlString;
	}

	private Logger logger = Logger.getLogger(Timeline.class);

	private boolean completedLoading = false;
	private List<String> enqueuedJs = new ArrayList<String>();

	public Timeline(Composite parent, int style) {
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
					if (!Timeline.this.getBrowser().execute(js)) {
						logger.error("Error occured while running JavaScript in browser: "
								+ js);
					}
				}
			}
		});

		try {
			String timelineUrlString = getFileUrl(Timeline.class,
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

	@Override
	public void show(final String jsonTimeline) {
		final String escapedJson = TimelineJsonGenerator.escape(jsonTimeline);
		final Runnable showRunnable = new Runnable() {
			@Override
			public void run() {
				if (!Timeline.this.getBrowser().isDisposed()) {
					Timeline.this.getBrowser().execute(
							"com.bkahlert.devel.nebula.timeline.loadJSON('"
									+ escapedJson + "');");
					Timeline.this.layout();
				}
			}
		};
		final Runnable run = new Runnable() {
			public void run() {
				if (completedLoading)
					showRunnable.run();
				else
					Timeline.this.getBrowser().addProgressListener(
							new ProgressAdapter() {
								@Override
								public void completed(ProgressEvent event) {
									showRunnable.run();
								}
							});
			};
		};
		if (Display.getCurrent() == Display.getDefault()) {
			run.run();
		} else {
			Display.getDefault().syncExec(run);
		}
	}

	public void show(ITimelineInput input, IProgressMonitor monitor) {
		String json = TimelineJsonGenerator.toJson(input, false, monitor);
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
			this.getBrowser()
					.execute(
							"com.bkahlert.devel.nebula.timeline.setCenterVisibleDate('"
									+ TimelineJsonGenerator.toISO8601(calendar)
									+ "');");
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

}
