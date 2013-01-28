package com.bkahlert.devel.nebula.widgets.timeline;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Instances of this class denote a timeline displayed by means of the SIMILE
 * Timeline and an integrated browser widget. It also facilitates the
 * interaction with timeline widget by providing a rich API.
 * <p>
 * Please note that this basic implementation only consumes string
 * representations of Java objects. In order to represent a date you need to
 * provide it formatted in ISO 8601.<br/>
 * <p>
 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
 * 1984-05-15T14:30:00+02:00. To make your life easier the static utility method
 * {@link #toISO8601(Calendar)} is provided.
 * 
 * @author bkahlert
 * 
 */
public class Timeline extends Composite {

	/**
	 * Converts a {@link Calendar} object to its ISO 8601 representation.
	 * {@link Date}s are not supported since they don't provide any
	 * {@link TimeZone} information.
	 * <p>
	 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
	 * 1984-05-15T14:30:00+02:00.
	 * 
	 * @param calendar
	 * @return
	 */
	public static String toISO8601(Calendar calendar) {
		SimpleDateFormat iso8601 = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ");
		iso8601.setTimeZone(calendar.getTimeZone());
		String missingDots = iso8601.format(calendar.getTime()).replace("GMT",
				"");
		return missingDots.substring(0, missingDots.length() - 2) + ":"
				+ missingDots.substring(missingDots.length() - 2);
	}

	/**
	 * Escapes quotes of a JSON string to it can be concatenated with JavaScript
	 * code without breaking the commands.
	 * 
	 * @param json
	 * @return
	 */
	public static String escape(String json) {
		return json.replace("'", "\\'").replace("\"", "\\\"");
	}

	public static String getFileUrl(Class<?> clazz, String clazzRelativePath)
			throws IOException {
		URL timelineUrl = FileLocator.toFileURL(clazz
				.getResource(clazzRelativePath));
		String timelineUrlString = timelineUrl.toString().replace("file:",
				"file://");
		return timelineUrlString;
	}

	/**
	 * A {@link HotZone} denotes a time span that should be magnified when
	 * rendered.
	 * 
	 * TODO Allow to define the magnification factor.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class HotZone {
		private String start;
		private String end;

		public HotZone(String startDateISO8601, String endDateISO8601) {
			this.start = startDateISO8601;
			this.end = endDateISO8601;
		}

		public String getStart() {
			return this.start;
		}

		public String getEnd() {
			return this.end;
		}
	}

	/**
	 * A {@link Decorator} denotes a time span on a time line that receives a
	 * different background color when rendered. You may optionally provide
	 * labels that will show up next to the time span's start and end.
	 * 
	 * @author bkahlert
	 * 
	 */
	public class Decorator {
		private String startDate;
		private String endDate;
		private String startLabel;
		private String endLabel;

		public Decorator(String startDateDateISO8601, String startLabel,
				String endDateDateISO8601, String endLabel) {
			this.startDate = startDateDateISO8601;
			this.endDate = endDateDateISO8601;
			this.startLabel = startLabel;
			this.endLabel = endLabel;
		}

		public Decorator(String startDateDateISO8601, String endDateDateISO8601) {
			this(startDateDateISO8601, null, endDateDateISO8601, null);
		}

		public String getStartDate() {
			return this.startDate;
		}

		public String getEndDate() {
			return this.endDate;
		}

		public String getStartLabel() {
			return this.startLabel;
		}

		public String getEndLabel() {
			return this.endLabel;
		}
	}

	private Logger logger = Logger.getLogger(Timeline.class);

	protected Browser browser;
	private boolean completedLoading = false;
	private List<String> enqueuedJs = new ArrayList<String>();

	public Timeline(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		browser = new Browser(this, SWT.BORDER);
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				completedLoading = true;
				for (Iterator<String> iterator = enqueuedJs.iterator(); iterator
						.hasNext();) {
					String js = iterator.next();
					iterator.remove();
					if (!browser.execute(js)) {
						logger.error("Error occured while running JavaScript in browser: "
								+ js);
					}
				}
			}
		});

		try {
			String timelineUrlString = getFileUrl(Timeline.class,
					"html/timeline.html");
			browser.setUrl(timelineUrlString + "?internal=true");
		} catch (IOException e) {
			logger.error("Could not open timeline html", e);
		}
	}

	/**
	 * Runs a Java script in the browser immediately.
	 * 
	 * @param js
	 * @return
	 */
	public boolean runJs(String js) {
		boolean success = browser.execute(js);
		if (!success) {
			logger.error("Error occured while running JavaScript in browser: "
					+ js);
		}
		return success;
	}

	/**
	 * Runs a Java script in the browser after the DOM has been loaded.
	 * 
	 * @param js
	 */
	public void enqueueJs(String js) {
		if (completedLoading) {
			runJs(js);
		} else {
			enqueuedJs.add(js);
		}
	}

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 */
	public void injectCssFile(String path) {
		String js = "if(document.createStyleSheet){document.createStyleSheet(\""
				+ path
				+ "\")}else{$(\"head\").append($(\"<link rel=\\\"stylesheet\\\" href=\\\""
				+ path + "\\\" type=\\\"text/css\\\" />\"))}";
		enqueueJs(js);
	}

	/**
	 * Display the given JSON string on the timeline. The json string must be of
	 * the following form whereas all fields of the option property and the
	 * option property are optional.<br>
	 * See <a href=
	 * "http://code.google.com/p/simile-widgets/wiki/Timeline_EventSources"
	 * >code.google.com/p/simile-widgets/wiki/Timeline_EventSources</a> for the
	 * complete specification.
	 * 
	 * <pre>
	 * <code>
	 * {
	 *   "options" : {
	 *     "zones" : [ {
	 *       "start" : "2011-09-13T12:05:22+01:00",
	 *       "end" : "2011-09-13T12-05-25+01:00"
	 *     } ],
	 *     "decorators" : ...
	 *     "centerDate" : "2011-09-13T12:05:22+01:00"
	 *   },
	 *   "events" : [ {
	 *     "title" : "TITLE",
	 *     "start" : "Sep 13 2011 14:05:22 GMT+0200",
	 *     "end" : "Sep 13 2011 14:05:31 GMT+0200",
	 *     "durationEvent" : true,
	 *     "icon" : "http://domain.tld/image_thumb.png",
	 *     "image" : "http://domain.tld/image.png"
	 *   }, ... ]
	 * }
	 * 
	 * </code>
	 * </pre>
	 * 
	 * 
	 * {@link HotZone}s and {@link Decorator}s provide classes than you can use
	 * in conjunction with a JSON generator like <a
	 * href="http://jackson.codehaus.org/">Jackson High-performance JSON
	 * processor</a>
	 * 
	 * <pre>
	 * <code>
	 * HashMap<String, Object> options = new HashMap<String, Object>();
	 * options.put("zones",
	 * 	new Timeline.HotZone[] {
	 * 		new Timeline.HotZone(
	 * 			"2011-09-13T18:10:01+02:00",
	 * 			"2011-09-15T18:10:01+02:00") 
	 * 	});
	 * options.put("decorators",
	 * 	new Timeline.Decorator[] {
	 * 		new Timeline.Decorator(
	 * 			"2011-09-13T18:20:01+02:00", "title1",
	 * 			"2011-09-13T18:00:01+02:00", "title2")
	 * 	});
	 * 
	 * </code>
	 * </pre>
	 * 
	 * @param jsonTimeline
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void show(final String jsonTimeline) {
		final String escapedJson = escape(jsonTimeline);
		final Runnable showRunnable = new Runnable() {
			@Override
			public void run() {
				if (browser != null && !browser.isDisposed()) {
					browser.execute("com.bkahlert.devel.nebula.timeline.loadJSON('"
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
					browser.addProgressListener(new ProgressAdapter() {
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

	/**
	 * Sets the date where the visible part of the {@link Timeline} should start
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMinVisibleDate(String iso8601Date) {
		if (browser != null && !isDisposed()) {
			browser.execute("com.bkahlert.devel.nebula.timeline.setMinVisibleDate('"
					+ iso8601Date + "');");
		}
	}

	/**
	 * Sets the date where the visible part of the {@link Timeline} should start
	 * 
	 * @param calendar
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMinVisibleDate(Calendar calendar) {
		this.setMinVisibleDate(toISO8601(calendar));
	}

	/**
	 * Sets the date where the visible part of the {@link Timeline} should
	 * centered
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setCenterVisibleDate(String iso8601Date) {
		if (browser != null && !isDisposed()) {
			browser.execute("com.bkahlert.devel.nebula.timeline.setCenterVisibleDate('"
					+ iso8601Date + "');");
		}
	}

	/**
	 * Sets the date where the visible part of the {@link Timeline} should end
	 * 
	 * @param calendar
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setCenterVisibleDate(Calendar calendar) {
		this.setMinVisibleDate(toISO8601(calendar));
	}

	/**
	 * Sets the date where the visible part of the {@link Timeline} should end
	 * 
	 * @param iso8601Date
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMaxVisibleDate(String iso8601Date) {
		if (browser != null && !isDisposed()) {
			browser.execute("com.bkahlert.devel.nebula.timeline.setMaxVisibleDate('"
					+ iso8601Date + "');");
		}
	}

	/**
	 * Sets the date where the visible part of the {@link Timeline} should end
	 * 
	 * @param calendar
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void setMaxVisibleDate(Calendar calendar) {
		this.setMinVisibleDate(toISO8601(calendar));
	}

	/**
	 * Renders the default {@link Decorator}s plus the given {@link Decorator}
	 * on this {@link Timeline}.
	 * 
	 * @param jsonDecorators
	 *            a list of {@link Decorator} as a json string; example: <code>
	 * [{ "startDate": "2011-09-13T13:08:05+02:00", "endDate": "2011-09-13T13:18:28+02:00" }]</code>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong
	 *                thread</li>
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been
	 *                disposed</li>
	 *                </ul>
	 */
	public void applyDecorators(String jsonDecorators) {
		if (browser != null && !isDisposed()) {
			browser.execute("com.bkahlert.devel.nebula.timeline.applyDecorators('"
					+ escape(jsonDecorators) + "');");
		}
	}
}
