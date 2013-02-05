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
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;

public class MultiSourceTimelineViewer implements IMultiSourceTimelineViewer {

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

	private ITimeline timeline;
	private Object input;

	private List<ITimelineContentProvider> contentProviders = new ArrayList<ITimelineContentProvider>();
	private List<ITimelineBandLabelProvider> bandLabelProviders = new ArrayList<ITimelineBandLabelProvider>();
	private List<ITimelineEventLabelProvider> eventLabelProviders = new ArrayList<ITimelineEventLabelProvider>();

	public MultiSourceTimelineViewer(ITimeline timeline) {
		this.timeline = timeline;
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
		List<Object[]> bands = new ArrayList<Object[]>();
		for (ITimelineContentProvider contentProvider : this.contentProviders) {
			// TODO use monitor
			bands.add(contentProvider.getBands(null));
			numBands += bands.get(bands.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands + 10);

		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int i = 0, m = bands.size(); i < m; i++) {
			ITimelineContentProvider contentProvider = this.contentProviders
					.get(i);
			ITimelineBandLabelProvider bandLabelProvider = this.bandLabelProviders
					.get(i);
			ITimelineEventLabelProvider eventLabelProvider = this.eventLabelProviders
					.get(i);

			for (Object band : bands.get(i)) {
				IOptions bandOptions = TimelineViewerHelper.getBandOptions(
						band, bandLabelProvider);
				List<ITimelineEvent> events = TimelineViewerHelper
						.getEvents(
								contentProvider.getEvents(band,
										subMonitor.newChild(1)),
								eventLabelProvider);
				ITimelineBand timelineBand = new TimelineBand(bandOptions,
						events);
				timelineBands.add(timelineBand);
			}
		}

		IOptions options = new Options();

		List<TimeZoneDateRange> ranges = new ArrayList<TimeZoneDateRange>();
		// FIXME
		for (ITimelineBand timelineBand : timelineBands) {
			TimeZoneDate end = timelineBand.getEnd() != null ? new TimeZoneDate(
					timelineBand.getEnd()) : null;
			ranges.add(new TimeZoneDateRange(start, end));
		}
		TimeZoneDateRange range = TimeZoneDateRange
				.calculateOuterDateRange(ranges
						.toArray(new TimeZoneDateRange[0]));

		ITimelineInput timelineInput = new TimelineInput(options, timelineBands);
		this.timeline.show(timelineInput, subMonitor.newChild(10));
	}

}
