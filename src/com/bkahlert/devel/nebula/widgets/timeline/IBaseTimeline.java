package com.bkahlert.devel.nebula.widgets.timeline;

import java.util.Calendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.services.IDisposable;

import com.bkahlert.devel.nebula.utils.CalendarUtils;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.model.IDecorator;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

/**
 * Instances of this class denote a {@link IBaseTimeline} displayed by means of
 * the SIMILE timeline and an integrated browser widget. It also facilitates the
 * interaction with timeline widget by providing a rich API.
 * <p>
 * Please note that this basic implementation only consumes string
 * representations of Java objects. In order to represent a date you need to
 * provide it formatted in ISO 8601.<br/>
 * <p>
 * e.g. Tuesday, 15 May 1984 at 2:30pm in timezone 01:00 summertime would be
 * 1984-05-15T14:30:00+02:00. To make your life easier the static utility method
 * {@link CalendarUtils#toISO8601(Calendar)} is provided.
 * 
 * @author bkahlert
 * 
 */
public interface IBaseTimeline extends IBrowserComposite, IDisposable {

	/**
	 * Runs a Java script in the browser immediately.
	 * <p>
	 * Warning: Calling this method does not guarantee that the DOM has been
	 * loaded, yet. Use {@link #enqueueJs(String)} if you want to make sure.
	 * 
	 * @param js
	 * @return
	 */
	@Override
	public boolean runJs(String js);

	/**
	 * Runs a Java script in the browser after the DOM has been loaded.
	 * 
	 * @param js
	 */
	@Override
	public void enqueueJs(String js);

	/**
	 * Includes the given path as a cascading style sheet.
	 * 
	 * @param path
	 */
	@Override
	public void injectCssFile(String path);

	/**
	 * Displays the given {@link ITimelineInput} on the {@link IBaseTimeline}.
	 * <p>
	 * May be called from whatever thread.
	 * 
	 * @param input
	 * @param monitor
	 */
	public void show(ITimelineInput input, IProgressMonitor monitor);

	/**
	 * Displays the given {@link ITimelineInput} on the {@link IBaseTimeline} by
	 * using an animation before and after the load process.
	 * <p>
	 * May be called from whatever thread.
	 * 
	 * @param input
	 * @param startAnimationDuration
	 *            duration the start animation takes; if <= 0 no animation will
	 *            occur
	 * @param endAnimationDuration
	 *            duration the end animation takes; if <= 0 no animation will
	 *            occur
	 * @param monitor
	 */
	public void show(ITimelineInput input, int startAnimationDuration,
			int endAnimationDuration, IProgressMonitor monitor);

	/**
	 * Sets the date where the visible part of the {@link IBaseTimeline} should
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
	 * Sets the date where the visible part of the {@link IBaseTimeline} should
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
	 * Gets the date where the visible part of the {@link IBaseTimeline} is
	 * currently centered.
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
	public Calendar getCenterVisibleDate();

	/**
	 * Sets the date where the visible part of the {@link IBaseTimeline} should
	 * end.
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
	 * Sets and zooms to the specified zoom index.
	 * 
	 * @param index
	 */
	public void setZoomIndex(int index);

	/**
	 * Gets the currently used zoom index.
	 * 
	 * @return null if timeline does not support zooming.
	 */
	public Integer getZoomIndex();

	/**
	 * Applies the given {@link IDecorator}s to the timeline. Replaces all
	 * decorations that existed before.
	 * <p>
	 * Hint: This method may be called from a non-UI thread. The relatively
	 * time-consuming JSON conversion is done asynchronously making this method
	 * return immediately.
	 * 
	 * @param decorators
	 */
	public void setDecorators(IDecorator[] decorators);

	/**
	 * Returns the currently applied decorations.
	 * 
	 * @return
	 */
	public IDecorator[] getDecorators();

	/**
	 * Returns the event that is the closest one to the given event (starting
	 * later).
	 * 
	 * @param event
	 * @return
	 */
	public ITimelineEvent getSuccessor(ITimelineEvent event);

	/**
	 * Returns the event that is the closest one to the given event (starting
	 * earlier).
	 * 
	 * @param event
	 * @return
	 */
	public ITimelineEvent getPredecessor(ITimelineEvent event);

}