package com.bkahlert.devel.nebula.viewer.timeline.provider.complex.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.viewer.timeline.impl.AbstractTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.impl.TimelineViewerHelper;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineGroup;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

public class TimelineProvider<TIMELINEGROUPVIEWER extends AbstractTimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT>, TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
		implements
		ITimelineProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> {
	private ITimelineLabelProvider<TIMELINE> timelineLabelProvider;
	private List<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandGroupProvider;

	public TimelineProvider(
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider,
			List<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> bandGroupProvider) {
		super();
		this.timelineLabelProvider = timelineLabelProvider;
		this.bandGroupProvider = bandGroupProvider;
	}

	@Override
	public ITimelineLabelProvider<TIMELINE> getTimelineLabelProvider() {
		return this.timelineLabelProvider;
	}

	@Override
	public List<IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT>> getBandGroupProviders() {
		return this.bandGroupProvider;
	}

	@Override
	public ITimelineInput generateTimelineInput(TIMELINE timeline,
			IProgressMonitor monitor) {
		return this.generateTimelineInput(timeline, null, monitor);
	}

	@Override
	public ITimelineInput generateTimelineInput(
			TIMELINE timeline,
			ITimelineProvider.ITimelineLabelProviderCreationInterceptor creationInterceptor,
			IProgressMonitor monitor) {
		int numBands = 0;
		List<Object[]> bandGroups = new ArrayList<Object[]>();
		for (IBandGroupProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> provider : this
				.getBandGroupProviders()) {
			// TODO use monitor
			bandGroups.add(provider.getContentProvider().getBands(null));
			numBands += bandGroups.get(bandGroups.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands);

		// create internal model
		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int bandGroupNumber = 0, m = bandGroups.size(); bandGroupNumber < m; bandGroupNumber++) {
			Object[] bandGroup = bandGroups.get(bandGroupNumber);

			ITimelineContentProvider<TIMELINEGROUPVIEWER, TIMELINEGROUP, TIMELINE, INPUT> contentProvider = this
					.getBandGroupProviders().get(bandGroupNumber)
					.getContentProvider();
			ITimelineBandLabelProvider bandLabelProvider = this
					.getBandGroupProviders().get(bandGroupNumber)
					.getBandLabelProvider();
			ITimelineEventLabelProvider eventLabelProvider = this
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
						if (creationInterceptor != null) {
							creationInterceptor.postProcess(event,
									timelineEvent);
						}
						currentTimelineEvents.add(timelineEvent);
					}
				}
				ITimelineBand timelineBand = new TimelineBand(bandOptions,
						currentTimelineEvents);
				timelineBands.add(timelineBand);
			}
		}

		IOptions options = TimelineViewerHelper.getTimelineOptions(timeline,
				this.getTimelineLabelProvider());
		return new TimelineInput(options, timelineBands);
	};

}