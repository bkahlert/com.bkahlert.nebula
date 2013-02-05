package com.bkahlert.devel.nebula.widgets.timeline.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.timeline.ISelectionTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;

public class SelectionTimeline extends Timeline implements ISelectionTimeline {

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
	private ITimelineListener timelineListener = new ITimelineListener() {
		@Override
		public void clicked(ITimelineEvent timelineEvent) {
			setSelection(new StructuredSelection(timelineEvent));
		}

		@Override
		public void middleClicked(ITimelineEvent timelineEvent) {
			setSelection(new StructuredSelection(timelineEvent));
		}

		@Override
		public void rightClicked(ITimelineEvent timelineEvent) {
			setSelection(new StructuredSelection(timelineEvent));
		}

		@Override
		public void doubleClicked(ITimelineEvent timelineEvent) {
			setSelection(new StructuredSelection(timelineEvent));
		}

		@Override
		public void hoveredIn(ITimelineEvent timelineEvent) {
		}

		@Override
		public void hoveredOut(ITimelineEvent timelineEvent) {
		}
	};

	private ISelection selection = null;
	private ListenerList seletionChangedListeners = new ListenerList();

	private ITimelineInput input;

	public SelectionTimeline(Composite parent, int style) {
		super(parent, style);

		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_click_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).clicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).middleClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_rclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).rightClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_dblclick_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).doubleClicked(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mouseIn_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).hoveredIn(event);
			}
		};
		new TimelineListenerBrowserFunction(this.getBrowser(),
				"timeline_plugin_mouseOut_callback") {
			@Override
			public void call(int bandNumber, int eventNumber) {
				Object[] listeners = timelineListeners.getListeners();
				ITimelineEvent event = input.getBand(bandNumber).getEvent(
						eventNumber);
				for (int i = 0, m = listeners.length; i < m; ++i)
					((ITimelineListener) listeners[i]).hoveredOut(event);
			}
		};

		String catchEventsJs = "jQuery(document).ready(function(e){function t(t,n){var r=document.elementFromPoint(t.pageX,t.pageY);if(!r)return false;var i=e(r);if(i.hasClass(\"timeline-event-tape\")){i=i.prev();if(!i){alert(\"No previous element found. This is unexpected since tapes should always be preceded by a label div.\")}}var s=false;e.each(i.attr(\"class\").split(/\\s+/),function(e,t){if(s)return false;if(t.length>\"_-_\".length*3){parts=t.split(\"_-_\");if(parts.length==4){s=true;n(parts[2])}}});if(!s)n(null);return false}e(document).click(function(e){switch(e.which){case 1:return t(e,timeline_plugin_click_callback);break;case 2:return t(e,timeline_plugin_mclick_callback);break;case 3:break;default:}return false});e(document).bind(\"contextmenu\",function(e){return t(e,timeline_plugin_rclick_callback)});e(document).dblclick(function(e){return t(e,timeline_plugin_dblclick_callback)});var n=null;e(document).mousemove(function(e){return t(e,function(e){if(n==e)return;if(n!=null)timeline_plugin_mouseOut_callback(n);if(e!=null)timeline_plugin_mouseIn_callback(e);n=e})})})";
		this.enqueueJs(catchEventsJs);

		this.addTimelineListener(timelineListener);

		addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				removeTimelineListener(timelineListener);
			}
		});
	}

	public void show(ITimelineInput input, IProgressMonitor monitor) {
		addIdentifiers(input);
		String json = TimelineJsonGenerator.toJson(input, false, monitor);
		System.err.println(json);
		this.show(json);
		this.input = input;
	}

	public void addTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.add(timelineListener);
	}

	public void removeTimelineListener(ITimelineListener timelineListener) {
		this.timelineListeners.remove(timelineListener);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (listener != null)
			this.seletionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (listener != null)
			this.seletionChangedListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
		SelectionChangedEvent event = new SelectionChangedEvent(this,
				this.selection);
		Object[] listeners = this.seletionChangedListeners.getListeners();
		for (Object listener : listeners) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}
}
