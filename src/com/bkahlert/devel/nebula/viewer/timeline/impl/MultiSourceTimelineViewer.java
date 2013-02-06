package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.viewer.timeline.IMultiSourceTimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;

/**
 * This
 * 
 * @author bkahlert
 * 
 */
public class MultiSourceTimelineViewer extends TimelineViewer implements
		IMultiSourceTimelineViewer {

	public class ProviderGroup implements IProviderGroup {

		private ITimelineContentProvider contentProvider;
		private ITimelineBandLabelProvider bandLabelProvider;
		private ITimelineEventLabelProvider eventLabelProvider;

		public ProviderGroup(ITimelineContentProvider contentProvider,
				ITimelineBandLabelProvider bandLabelProvider,
				ITimelineEventLabelProvider eventLabelProvider) {
			super();
			this.contentProvider = contentProvider;
			this.bandLabelProvider = bandLabelProvider;
			this.eventLabelProvider = eventLabelProvider;
		}

		@Override
		public ITimelineContentProvider getContentProvider() {
			return this.contentProvider;
		}

		@Override
		public ITimelineBandLabelProvider getBandLabelProvider() {
			return this.bandLabelProvider;
		}

		@Override
		public ITimelineEventLabelProvider getEventLabelProvider() {
			return this.eventLabelProvider;
		}

	}

	private Object input;

	private List<ITimelineContentProvider> contentProviders = new ArrayList<ITimelineContentProvider>();
	private List<ITimelineBandLabelProvider> bandLabelProviders = new ArrayList<ITimelineBandLabelProvider>();
	private List<ITimelineEventLabelProvider> eventLabelProviders = new ArrayList<ITimelineEventLabelProvider>();

	public MultiSourceTimelineViewer(ITimeline timeline) {
		super(timeline);
	}

	@Override
	public void setInput(Object input) {
		if (this.input != input) {
			Object oldInput = this.input;
			Object newInput = input;
			this.input = input;
			for (ITimelineContentProvider contentProvider : this.contentProviders) {
				contentProvider.inputChanged(this, oldInput, newInput);
			}
		}
	}

	@Override
	public Object getInput() {
		return this.input;
	}

	@Override
	public void setProviders(IProviderGroup[] providers) {
		this.contentProviders.clear();
		this.bandLabelProviders.clear();
		this.eventLabelProviders.clear();

		if (providers != null) {
			for (IProviderGroup provider : providers) {
				this.contentProviders.add(provider.getContentProvider());
				this.bandLabelProviders.add(provider.getBandLabelProvider());
				this.eventLabelProviders.add(provider.getEventLabelProvider());
			}
		}

		for (ITimelineContentProvider contentProvider : this.contentProviders) {
			contentProvider.inputChanged(this, null, this.input);
		}
	}

	public void refresh(IProgressMonitor monitor) {
		int numBands = 0;
		List<Object[]> bandGroups = new ArrayList<Object[]>();
		for (ITimelineContentProvider contentProvider : this.contentProviders) {
			// TODO use monitor
			bandGroups.add(contentProvider.getBands(null));
			numBands += bandGroups.get(bandGroups.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands + 10);

		// create internal model
		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int bandGroupNumber = 0, m = bandGroups.size(); bandGroupNumber < m; bandGroupNumber++) {
			Object[] bandGroup = bandGroups.get(bandGroupNumber);

			ITimelineContentProvider contentProvider = this.contentProviders
					.get(bandGroupNumber);
			ITimelineBandLabelProvider bandLabelProvider = this.bandLabelProviders
					.get(bandGroupNumber);
			ITimelineEventLabelProvider eventLabelProvider = this.eventLabelProviders
					.get(bandGroupNumber);

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

		IOptions options = TimelineViewerHelper.getTimelineOptions(this
				.getTimelineLabelProvider());
		ITimelineInput timelineInput = new TimelineInput(options, timelineBands);
		((ITimeline) this.getControl()).show(timelineInput,
				subMonitor.newChild(10));
	}

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
