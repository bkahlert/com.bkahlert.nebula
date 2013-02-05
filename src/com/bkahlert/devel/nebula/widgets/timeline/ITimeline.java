package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;

import org.eclipse.swt.SWTException;

import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Decorator;
import com.bkahlert.devel.nebula.widgets.timeline.impl.HotZone;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Timeline;

/**
 * Instances of this class denote a timeline displayed by means of the SIMILE
 * SelectionTimeline and an integrated browser widget. It also facilitates the
 * interaction with timeline widget by providing a rich API.
 * <p>
 * Please note that this basic implementation only consumes string
 * representations of Java objects. In order to represent a date you need to
 * provide it formatted in ISO 8601.<br/>
 * <p>
 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
 * 1984-05-15T14:30:00+02:00. To make your life easier the static utility method
 * {@link TimelineJsonGenerator#toISO8601(Calendar)} is provided.
 * 
 * @author bkahlert
 * 
 */
public interface ITimeline extends IBrowserComposite {

	/**
	 * Runs a Java script in the browser immediately.
	 * <p>
	 * Warning: Calling this method does not guarantee that the DOM has been
	 * loaded, yet. Use {@link #enqueueJs(String)} if you want to make sure.
	 * 
	 * @param js
	 * @return
	 */
	public boolean runJs(String js);

	/**
	 * Runs a Java script in the browser after the DOM has been loaded.
	 * 
	 * @param js
	 */
	public void enqueueJs(String js);

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 */
	public void injectCssFile(String path);

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
	 * 	new SelectionTimeline.HotZone[] {
	 * 		new SelectionTimeline.HotZone(
	 * 			"2011-09-13T18:10:01+02:00",
	 * 			"2011-09-15T18:10:01+02:00") 
	 * 	});
	 * options.put("decorators",
	 * 	new SelectionTimeline.Decorator[] {
	 * 		new SelectionTimeline.Decorator(
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
	public void show(final String jsonTimeline);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should
	 * start.
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
	public void setMinVisibleDate(Calendar calendar);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should
	 * centered.
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
	public void setCenterVisibleDate(Calendar calendar);

	/**
	 * Sets the date where the visible part of the {@link Timeline} should end.
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
	public void setMaxVisibleDate(Calendar calendar);

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
	public void applyDecorators(String jsonDecorators);
}