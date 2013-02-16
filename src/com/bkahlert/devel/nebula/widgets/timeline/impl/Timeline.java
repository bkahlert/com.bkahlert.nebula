package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

public class Timeline extends BaseTimeline implements ITimeline {

	private static String separator = "_-_";

	/**
	 * Adds an identifier to each event's class name.
	 * 
	 * @param input
	 */
	private static void addIdentifiers(ITimelineInput input) {
		for (int i = 0, m = input.getBandCount(); i < m; i++) {
			ITimelineBand band = input.getBands().get(i);
			for (int j = 0, n = band.getEventCount(); j < n; j++) {
				ITimelineEvent event = band.getEvents().get(j);
				event.addClassName(separator + "timeline-id" + separator + i
						+ ',' + j + separator);
			}
		}
	}

	public static abstract class TimelineListenerBrowserFunction extends
			BrowserFunction {
		/**
		 * Extracts the band and event number from an arguments array such as
		 * ["3,13", ...].
		 * 
		 * @param id
		 * @return
		 */
		public static int[] fromArguments(Object[] arguments) {
			if (arguments.length == 1 && arguments[0] instanceof String) {
				String[] parts = ((String) arguments[0]).split(",");
				try {
					int bandNumber = Integer.parseInt(parts[0]);
					int eventNumber = Integer.parseInt(parts[1]);
					return new int[] { bandNumber, eventNumber };
				} catch (Exception e) {

				}
			}
			;
			return null;
		}

		public TimelineListenerBrowserFunction(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] arguments) {
			int[] id = fromArguments(arguments);
			if (id != null)
				call(id[0], id[1]);
			return null;
		}

		public abstract void call(int bandNumber, int eventNumber);
	}

	private ListenerList timelineListeners = new ListenerList();

	private ITimelineInput input;

	public Timeline(Composite parent, int style) {
		super(parent, style);

		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_click_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).clicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).middleClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_rclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).rightClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_dblclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).doubleClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mouseIn_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).hoveredIn(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mouseOut_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				TimelineEvent event = new TimelineEvent(Display.getCurrent(),
						Timeline.this, input.getBand(bandNumber).getEvent(
								eventNumber));
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).hoveredOut(event);
			}
		};

		String catchEventsJs = "jQuery(document).ready(function(e){function t(t,n){var r=document.elementFromPoint(t.pageX,t.pageY);if(!r)return false;var i=e(r);if(i.hasClass(\"timeline-event-tape\")){i=i.prev();if(!i){alert(\"No previous element found. This is unexpected since tapes should always be preceded by a label div.\")}}var s=false;e.each(i.attr(\"class\").split(/\\s+/),function(e,t){if(s)return false;if(t.length>\"_-_\".length*3){parts=t.split(\"_-_\");if(parts.length==4){s=true;n(parts[2])}}});if(!s)n(null);return false}e(document).click(function(e){switch(e.which){case 1:return t(e,timeline_plugin_click_callback);break;case 2:return t(e,timeline_plugin_mclick_callback);break;case 3:break;default:}return false});e(document).bind(\"contextmenu\",function(e){return t(e,timeline_plugin_rclick_callback)});e(document).dblclick(function(e){return t(e,timeline_plugin_dblclick_callback)});var n=null;e(document).mousemove(function(e){return t(e,function(e){if(n==e)return;if(n!=null)timeline_plugin_mouseOut_callback(n);if(e!=null)timeline_plugin_mouseIn_callback(e);n=e})})})";
		this.enqueueJs(catchEventsJs);
	}

	public void show(ITimelineInput input, IProgressMonitor monitor) {
		addIdentifiers(input);
		this.input = input;
		super.show(input, monitor);
	}

	@Override
	public void show(ITimelineInput input, int startAnimationDuration,
			int endAnimationDuration, IProgressMonitor monitor) {
		addIdentifiers(input);
		this.input = input;
		super.show(input, startAnimationDuration, endAnimationDuration, monitor);
	}

	private int getIndex(Object event) {
		if (getSortedEvents() != null) {
			for (int i = 0, m = getSortedEvents().size(); i < m; i++) {
				ITimelineEvent timelineEvent = getSortedEvents().get(i);
				if (timelineEvent.getPayload() != null
						&& timelineEvent.getPayload().equals(event)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public Object getSuccessor(Object event) {
		int index = getIndex(event);
		if (index > -1) {
			ITimelineEvent timelineEvent = getSortedEvents().get(index);
			ITimelineEvent successorEvent = getSuccessor(timelineEvent);
			return successorEvent != null ? successorEvent.getPayload() : null;
		}
		return null;
	}

	@Override
	public Object getPredecessor(Object event) {
		int index = getIndex(event);
		if (index > -1) {
			ITimelineEvent timelineEvent = getSortedEvents().get(index);
			ITimelineEvent predecessorEvent = getPredecessor(timelineEvent);
			return predecessorEvent != null ? predecessorEvent.getPayload()
					: null;
		}
		return null;
	}

	@Override
	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	@Override
	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

}
