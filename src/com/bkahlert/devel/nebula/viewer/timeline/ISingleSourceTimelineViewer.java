package com.bkahlert.devel.nebula.viewer.timeline;

public interface ISingleSourceTimelineViewer extends ITimelineViewer {

	public void setContentProvider(ITimelineContentProvider contentProvider);

	public void setBandLabelProvider(
			ITimelineBandLabelProvider bandLabelProvider);

	public void setEventLabelProvider(
			ITimelineEventLabelProvider eventLabelProvider);

}