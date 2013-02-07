package com.bkahlert.devel.nebula.viewer.timelineGroup.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IBaseLabelProvider;

import com.bkahlert.devel.nebula.viewer.timeline.impl.TimelineViewerHelper;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProviders;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProviderFactory;
import com.bkahlert.devel.nebula.viewer.timelineGroup.IMultiSourceTimelineGroupViewer;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

/**
 * This
 * 
 * @author bkahlert
 * 
 */
public class MultiSourceTimelineGroupViewer<TIMELINE extends ITimeline> extends
		TimelineGroupViewer<TIMELINE> implements
		IMultiSourceTimelineGroupViewer<TIMELINE> {

	/**
	 * Detects if an object is actually some kind of array or collection and
	 * returns a list containing the found elements.
	 * 
	 * @param input
	 * @return
	 */
	public static List<Object> extractInputs(Object input) {
		List<Object> timelineInputs = new LinkedList<Object>();
		if (input.getClass().isArray())
			timelineInputs.addAll(Arrays.asList((Object[]) input));
		if (input instanceof Collection<?>)
			timelineInputs.addAll((Collection<?>) input);
		return timelineInputs;
	}

	private Object input;

	private ITimelineProviderFactory<TIMELINE> timelineProviderFactory = null;

	private static class Info {
		public Object input;
		public ITimelineViewer viewer;
	}

	/**
	 * Contains the currently used {@link ITimelineProvider} and their inputs
	 * and viewers.
	 */
	private Map<ITimelineProvider<TIMELINE>, Info> timelineProviderInfos = new HashMap<ITimelineProvider<TIMELINE>, Info>();

	public MultiSourceTimelineGroupViewer(ITimelineGroup<TIMELINE> timelineGroup) {
		super(timelineGroup);
	}

	@Override
	// FIXME input auf list testen und nur ein element je contentprovider geben
	public void setInput(Object input) {
		if (this.input != input) {
			Object oldInput = this.input;
			Object newInput = input;
			this.input = input;

			ITimelineGroup<TIMELINE> timelineGroup = (ITimelineGroup<TIMELINE>)this.getControl();
			timelineGroup.
			
			if (this.timelineProviderFactory != null) {
				List<Object> timelineInputs = extractInputs(input);
				for (Object timelineInput : timelineInputs) {
					ITimelineProvider<TIMELINE> timelineProvider = this.timelineProviderFactory
							.createTimelineGroupProvider();
					for (IBandGroupProviders bandGroupProviders : timelineProvider
							.getBandGroupProviders()) {
						bandGroupProviders.getContentProvider().inputChanged(
								this, oldInput, newInput);
					}
				}
			} else {
				for(ITimelineProvider<TIMELINE> timelineProvider : timelineProviderInfos.keySet()) {
					for (IBandGroupProviders bandGroupProviders : timelineProvider
							.getBandGroupProviders()) {
						bandGroupProviders.getContentProvider().inputChanged(timelineProviderViewers.get(timelineProvider), timelineProviderInputs.get(timelineProvider), null);
					}
				}
				timelineProviderInputs.clear();
				timelineProviderViewers.clear();
			}
		}
	}

	@Override
	public Object getInput() {
		return this.input;
	}

	// FIXME s.o.
	@Override
	public void setTimelineProviderFactory(
			ITimelineProviderFactory<TIMELINE> timelineProviderFactory) {
		this.timelineProviderFactory = timelineProviderFactory;

		for (ITimelineProvider provider : this.timelineProviderFactory) {
			for (IBandGroupProviders bandGroupProviders : provider
					.getBandGroupProviders()) {
				bandGroupProviders.getContentProvider().inputChanged(this,
						null, this.input);
			}
		}
	}

	public void refresh(IProgressMonitor monitor) {
		int numBands = 0;
		List<Object[]> bandGroups = new ArrayList<Object[]>();
		for (IBandGroupProviders provider : this.timelineProviderFactory) {
			// TODO use monitor
			bandGroups.add(provider.getContentProvider().getBands(null));
			numBands += bandGroups.get(bandGroups.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands + 10);

		// create internal model
		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int bandGroupNumber = 0, m = bandGroups.size(); bandGroupNumber < m; bandGroupNumber++) {
			Object[] bandGroup = bandGroups.get(bandGroupNumber);

			ITimelineContentProvider contentProvider = this.timelineProviderFactory
					.get(bandGroupNumber).getContentProvider();
			ITimelineBandLabelProvider bandLabelProvider = this.timelineProviderFactory
					.get(bandGroupNumber).getBandLabelProvider();
			ITimelineEventLabelProvider eventLabelProvider = this.timelineProviderFactory
					.get(bandGroupNumber).getEventLabelProvider();

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

	@Override
	public ITimelineLabelProvider getTimelineLabelProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTimelineLabelProvider(
			ITimelineLabelProvider timelineLabelProvider) {
		// TODO Auto-generated method stub

	}

}
