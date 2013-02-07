package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.viewer.timeline.IMultiSourceTimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProviders;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

/**
 * This
 * 
 * @author bkahlert
 * 
 */
public class MultiSourceTimelineViewer<TIMELINE extends ITimeline> extends
		TimelineViewer<TIMELINE> implements
		IMultiSourceTimelineViewer<TIMELINE> {

	private Object input;

	private ITimelineProvider<TIMELINE> timelineProvider = null;

	public MultiSourceTimelineViewer(final TIMELINE timeline) {
		super(timeline);
		Runnable addDisposeListener = new Runnable() {
			@Override
			public void run() {
				timeline.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						notifyInputChanged(
								MultiSourceTimelineViewer.this.input, null);
					}
				});
			}
		};
		if (Display.getCurrent() == Display.getDefault())
			addDisposeListener.run();
		else
			Display.getDefault().syncExec(addDisposeListener);
	}

	@Override
	public void setInput(Object input) {
		if (this.input != input) {
			Object oldInput = this.input;
			Object newInput = input;
			this.input = input;
			notifyInputChanged(oldInput, newInput);
		}
	}

	@Override
	public Object getInput() {
		return this.input;
	}

	@Override
	public void setTimelineProvider(ITimelineProvider<TIMELINE> timelineProvider) {
		notifyInputChanged(this.input, null);
		this.timelineProvider = timelineProvider;
		notifyInputChanged(null, this.input);
	}

	protected void notifyInputChanged(Object oldInput, Object newInput) {
		if (this.timelineProvider != null) {
			for (IBandGroupProviders bandGroupProvider : this.timelineProvider
					.getBandGroupProviders()) {
				bandGroupProvider.getContentProvider().inputChanged(this,
						oldInput, newInput);
			}
		}
	}

	public void refresh(IProgressMonitor monitor) {
		if (this.timelineProvider == null)
			return;

		int numBands = 0;
		List<Object[]> bandGroups = new ArrayList<Object[]>();
		for (IBandGroupProviders provider : this.timelineProvider
				.getBandGroupProviders()) {
			// TODO use monitor
			bandGroups.add(provider.getContentProvider().getBands(null));
			numBands += bandGroups.get(bandGroups.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands + 10);

		// create internal model
		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int bandGroupNumber = 0, m = bandGroups.size(); bandGroupNumber < m; bandGroupNumber++) {
			Object[] bandGroup = bandGroups.get(bandGroupNumber);

			ITimelineContentProvider contentProvider = this.timelineProvider
					.getBandGroupProviders().get(bandGroupNumber)
					.getContentProvider();
			ITimelineBandLabelProvider bandLabelProvider = this.timelineProvider
					.getBandGroupProviders().get(bandGroupNumber)
					.getBandLabelProvider();
			ITimelineEventLabelProvider eventLabelProvider = this.timelineProvider
					.getBandGroupProviders().get(bandGroupNumber)
					.getEventLabelProvider();

			for (Object band : bandGroup) {
				IOptions bandOptions = TimelineViewerHelper.getBandOptions(
						band, bandLabelProvider);

				List<ITimelineEvent> currentTimelineEvents = new ArrayList<ITimelineEvent>();
				if (eventLabelProvider != null) {
					Object[] events = contentProvider.getEvents(band,
							subMonitor.newChild(1));
					for (Object event : events) {
						ITimelineEvent timelineEvent = TimelineViewerHelper
								.getEvent(event, eventLabelProvider);
						currentTimelineEvents.add(timelineEvent);
					}
				}
				ITimelineBand timelineBand = new TimelineBand(bandOptions,
						currentTimelineEvents);
				timelineBands.add(timelineBand);
			}
		}

		@SuppressWarnings("unchecked")
		IOptions options = TimelineViewerHelper.getTimelineOptions(
				(TIMELINE) getControl(),
				this.timelineProvider.getTimelineLabelProvider());
		ITimelineInput timelineInput = new TimelineInput(options, timelineBands);
		((ITimeline) this.getControl()).show(timelineInput,
				subMonitor.newChild(10));
	}

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
