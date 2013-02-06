package com.bkahlert.devel.nebula.viewer.timeline.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.bkahlert.devel.nebula.viewer.timeline.ISingleSourceTimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineContentProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineInput;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineBand;
import com.bkahlert.devel.nebula.widgets.timeline.impl.TimelineInput;

public class SingleSourceTimelineViewer extends TimelineViewer implements
		ITimelineViewer, ISingleSourceTimelineViewer {

	private ITimeline timeline;
	private Object input;

	private ITimelineContentProvider contentProvider;
	private ITimelineBandLabelProvider bandLabelProvider;
	private ITimelineEventLabelProvider eventLabelProvider;

	public SingleSourceTimelineViewer(ITimeline timeline) {
		this.timeline = timeline;
	}

	@Override
	public void setInput(Object input) {
		if (this.input != input) {
			Object oldInput = this.input;
			Object newInput = input;
			this.input = input;
			if (this.contentProvider != null) {
				this.contentProvider.inputChanged(this, oldInput, newInput);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.viewer.timeline.ISingleSourceTimelineViewer
	 * #setContentProvider
	 * (com.bkahlert.devel.nebula.viewer.timeline.ITimelineContentProvider)
	 */
	@Override
	public void setContentProvider(ITimelineContentProvider contentProvider) {
		this.contentProvider = contentProvider;
		this.contentProvider.inputChanged(this, null, this.input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.viewer.timeline.ISingleSourceTimelineViewer
	 * #setBandLabelProvider
	 * (com.bkahlert.devel.nebula.viewer.timeline.ITimelineBandLabelProvider)
	 */
	@Override
	public void setBandLabelProvider(
			ITimelineBandLabelProvider bandLabelProvider) {
		this.bandLabelProvider = bandLabelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.bkahlert.devel.nebula.viewer.timeline.ISingleSourceTimelineViewer
	 * #setEventLabelProvider
	 * (com.bkahlert.devel.nebula.viewer.timeline.ITimelineEventLabelProvider)
	 */
	@Override
	public void setEventLabelProvider(
			ITimelineEventLabelProvider eventLabelProvider) {
		this.eventLabelProvider = eventLabelProvider;
	}

	public void refresh(IProgressMonitor monitor) {
		Object[] bands = this.contentProvider.getBands(monitor);
		SubMonitor subMonitor = SubMonitor.convert(monitor, bands.length + 10);
		if (this.contentProvider == null)
			return;

		List<ITimelineBand> timelineBands = new ArrayList<ITimelineBand>();
		for (Object band : bands) {
			IOptions bandOptions = TimelineViewerHelper.getBandOptions(band,
					this.bandLabelProvider);
			List<ITimelineEvent> events = TimelineViewerHelper
					.getEvents(
							this.contentProvider.getEvents(band,
									subMonitor.newChild(1)),
							this.eventLabelProvider);
			ITimelineBand timelineBand = new TimelineBand(bandOptions, events);
			timelineBands.add(timelineBand);
		}

		IOptions options = getTimelineOptions();
		ITimelineInput timelineInput = new TimelineInput(options, timelineBands);
		this.timeline.show(timelineInput, subMonitor.newChild(10));
	}

}
