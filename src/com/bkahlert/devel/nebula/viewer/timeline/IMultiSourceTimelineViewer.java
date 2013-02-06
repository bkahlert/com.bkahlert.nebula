package com.bkahlert.devel.nebula.viewer.timeline;

/**
 * Interface for {@link ITimelineViewer} using multiple sets of label providers.
 * 
 * @author bkahlert
 * 
 */
public interface IMultiSourceTimelineViewer extends ITimelineViewer {

	public static interface IProviderGroup {
		public ITimelineContentProvider getContentProvider();

		public ITimelineBandLabelProvider getBandLabelProvider();

		public ITimelineEventLabelProvider getEventLabelProvider();
	}

	public void setProviders(IProviderGroup[] providers);

}