package com.bkahlert.devel.nebula.viewer.timeline;

public interface IMultiSourceTimelineViewer extends ITimelineViewer {

	public static interface IProviderGroup {
		public ITimelineContentProvider getContentProvider();

		public ITimelineBandLabelProvider getBandLabelProvider();

		public ITimelineEventLabelProvider getEventLabelProvider();
	}

	public void setProviders(IProviderGroup[] providers);

}