package com.bkahlert.nebula.viewer.timeline.provider.complex.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineBandLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineContentProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineEventLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.atomic.ITimelineLabelProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.impl.Options;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineEvent;
import com.bkahlert.nebula.widgets.timeline.impl.TimelineInput;
import com.bkahlert.nebula.widgets.timeline.model.IOptions;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineBand;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineEvent;
import com.bkahlert.nebula.widgets.timeline.model.ITimelineInput;

public class TimelineProvider<TIMELINE extends IBaseTimeline, INPUT> implements
		ITimelineProvider<TIMELINE, INPUT> {

	public static <TIMELINE extends IBaseTimeline, INPUT> ITimelineInput getTimelineInput(
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider,
			List<IBandGroupProvider<INPUT>> bandGroupProviders,
			TIMELINE timeline,
			ITimelineProvider.ITimelineLabelProviderCreationInterceptor creationInterceptor,
			IProgressMonitor monitor) {
		int numBands = 0;
		List<Object[]> bandGroups = new ArrayList<Object[]>();
		for (IBandGroupProvider<INPUT> provider : bandGroupProviders) {
			// TODO use monitor
			bandGroups.add(provider.getContentProvider().getBands(null));
			numBands += bandGroups.get(bandGroups.size() - 1).length;
		}

		SubMonitor subMonitor = SubMonitor.convert(monitor, numBands);

		// create internal model
		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (int bandGroupNumber = 0, m = bandGroups.size(); bandGroupNumber < m; bandGroupNumber++) {
			Object[] bandGroup = bandGroups.get(bandGroupNumber);

			ITimelineContentProvider<INPUT> contentProvider = bandGroupProviders
					.get(bandGroupNumber).getContentProvider();
			ITimelineBandLabelProvider bandLabelProvider = bandGroupProviders
					.get(bandGroupNumber).getBandLabelProvider();
			ITimelineEventLabelProvider eventLabelProvider = bandGroupProviders
					.get(bandGroupNumber).getEventLabelProvider();

			for (Object band : bandGroup) {
				IOptions bandOptions = TimelineProvider.getBandOptions(band,
						bandLabelProvider);

				List<ITimelineEvent> currentTimelineEvents = new ArrayList<ITimelineEvent>();
				if (eventLabelProvider != null) {
					Object[] events = contentProvider.getEvents(band,
							subMonitor.newChild(1));
					for (Object event : events) {
						ITimelineEvent timelineEvent = TimelineProvider
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

		IOptions options = TimelineProvider.getTimelineOptions(timeline,
				timelineLabelProvider);
		return new TimelineInput(options, timelineBands);
	}

	/**
	 * Generates the options for a {@link ITimeline} using a given
	 * {@link ITimelineLabelProvider}.
	 * 
	 * @param timelineLabelProvider
	 * @return
	 */
	public static <TIMELINE extends IBaseTimeline> IOptions getTimelineOptions(
			TIMELINE timeline,
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider) {
		IOptions options = new Options();
		if (timelineLabelProvider != null) {
			options.setTitle(timelineLabelProvider.getTitle(timeline));
			options.setCenterStart(timelineLabelProvider
					.getCenterStart(timeline));
			options.setTapeImpreciseOpacity(timelineLabelProvider
					.getTapeImpreciseOpacity(timeline));
			options.setIconWidth(timelineLabelProvider.getIconWidth(timeline));

			String[] bubbleFunction = timelineLabelProvider
					.getBubbleFunction(timeline);
			String functionName = bubbleFunction != null
					&& bubbleFunction.length > 0 ? bubbleFunction[0] : null;
			String functionField = bubbleFunction != null
					&& bubbleFunction.length > 1 ? bubbleFunction[1] : null;
			options.setBubbleFunction(functionName, functionField);

			options.setHotZones(timelineLabelProvider.getHotZones(timeline));
			options.setPermanentDecorators(timelineLabelProvider
					.getDecorators(timeline));
			options.setZoomSteps(timelineLabelProvider.getZoomSteps(timeline));
			options.setZoomIndex(timelineLabelProvider.getZoomIndex(timeline));
			options.setTimeZone(timelineLabelProvider.getTimeZone(timeline));
		}
		return options;
	}

	/**
	 * Generates the options for a specified band using a given
	 * {@link ITimelineBandLabelProvider}.
	 * 
	 * @param band
	 *            the object representing the band
	 * @param bandLabelProvider
	 *            the provider to ask
	 * @return
	 */
	public static IOptions getBandOptions(Object band,
			ITimelineBandLabelProvider bandLabelProvider) {
		IOptions bandOptions = new Options();

		if (bandLabelProvider != null) {
			String title = bandLabelProvider.getTitle(band);
			if (title != null) {
				bandOptions.setTitle(title);
			}

			Boolean isShowInOverviewBands = bandLabelProvider
					.isShowInOverviewBands(band);
			if (isShowInOverviewBands != null) {
				bandOptions.setShowInOverviewBands(isShowInOverviewBands);
			}

			Float ratio = bandLabelProvider.getRatio(band);
			if (ratio != null) {
				bandOptions.setRatio(ratio);
			}
		}

		return bandOptions;
	}

	/**
	 * Takes a value object and wraps it in a {@link ITimelineEvent} using the
	 * given {@link ITimelineEventLabelProvider}.
	 * 
	 * @param events
	 * @param eventLabelProvider
	 * @param additionalClassName
	 * @return
	 */
	public static ITimelineEvent getEvent(Object event,
			ITimelineEventLabelProvider eventLabelProvider) {
		if (eventLabelProvider == null) {
			return null;
		}

		String title = eventLabelProvider.getTitle(event);
		String tooltip = eventLabelProvider.getTooltip(event);
		URI icon = eventLabelProvider.getIcon(event);
		URI image = eventLabelProvider.getIcon(event);
		Calendar start = eventLabelProvider.getStart(event);
		Calendar end = eventLabelProvider.getEnd(event);
		RGB[] colors = eventLabelProvider.getColors(event);
		boolean resizable = eventLabelProvider.isResizable(event);
		String[] classNames = eventLabelProvider.getClassNames(event);

		ITimelineEvent timelineEvent = new TimelineEvent(title, tooltip, icon,
				image, start, end, colors, resizable, classNames, event);
		return timelineEvent;
	}

	private final ITimelineLabelProvider<TIMELINE> timelineLabelProvider;
	private final List<IBandGroupProvider<INPUT>> bandGroupProviders;

	public TimelineProvider(
			ITimelineLabelProvider<TIMELINE> timelineLabelProvider,
			List<IBandGroupProvider<INPUT>> bandGroupProviders) {
		super();
		this.timelineLabelProvider = timelineLabelProvider;
		this.bandGroupProviders = bandGroupProviders;
	}

	@Override
	public ITimelineLabelProvider<TIMELINE> getTimelineLabelProvider() {
		return this.timelineLabelProvider;
	}

	@Override
	public List<IBandGroupProvider<INPUT>> getBandGroupProviders() {
		return this.bandGroupProviders;
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
		return getTimelineInput(this.timelineLabelProvider,
				this.bandGroupProviders, timeline, creationInterceptor, monitor);
	};

}