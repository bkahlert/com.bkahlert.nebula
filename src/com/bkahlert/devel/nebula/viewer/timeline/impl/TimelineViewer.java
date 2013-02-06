package com.bkahlert.devel.nebula.viewer.timeline.impl;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.IOptions;
import com.bkahlert.devel.nebula.widgets.timeline.impl.Options;

public abstract class TimelineViewer implements ITimelineViewer {
	private ITimelineLabelProvider timelineLabelProvider;

	@Override
	public void setTimelineLabelProvider(
			ITimelineLabelProvider timelineLabelProvider) {
		this.timelineLabelProvider = timelineLabelProvider;
	}

	@Override
	public ITimelineLabelProvider getTimelineLabelProvider() {
		return timelineLabelProvider;
	}

	public IOptions getTimelineOptions() {
		IOptions options = new Options();
		if (this.timelineLabelProvider != null) {
			options.setTitle(this.timelineLabelProvider.getTitle());
			options.setCenterStart(this.timelineLabelProvider.getCenterStart());
			options.setTapeImpreciseOpacity(this.timelineLabelProvider
					.getTapeImpreciseOpacity());
			options.setIconWidth(this.timelineLabelProvider.getIconWidth());

			String[] bubbleFunction = this.timelineLabelProvider
					.getBubbleFunction();
			String functionName = bubbleFunction != null
					&& bubbleFunction.length > 0 ? bubbleFunction[0] : null;
			String functionField = bubbleFunction != null
					&& bubbleFunction.length > 1 ? bubbleFunction[1] : null;
			options.setBubbleFunction(functionName, functionField);

			options.setHotZones(this.timelineLabelProvider.getHotZones());
			options.setDecorators(this.timelineLabelProvider.getDecorators());
			options.setTimeZone(this.timelineLabelProvider.getTimeZone());
		}
		return options;
	}
}
