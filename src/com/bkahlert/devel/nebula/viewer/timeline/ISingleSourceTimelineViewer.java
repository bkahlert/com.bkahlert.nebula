package com.bkahlert.devel.nebula.viewer.timeline;

/**
 * Interface for {@link ITimelineViewer} using one set of label providers.
 * 
 * @author bkahlert
 * 
 */
public interface ISingleSourceTimelineViewer extends ITimelineViewer {

	public void setContentProvider(ITimelineContentProvider contentProvider);

	public void setBandLabelProvider(
			ITimelineBandLabelProvider bandLabelProvider);

	public void setEventLabelProvider(
			ITimelineEventLabelProvider eventLabelProvider);

}