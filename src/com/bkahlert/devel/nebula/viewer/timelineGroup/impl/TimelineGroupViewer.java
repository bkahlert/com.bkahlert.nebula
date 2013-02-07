package com.bkahlert.devel.nebula.viewer.timelineGroup.impl;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.viewer.timelineGroup.ITimelineGroupViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.devel.nebula.widgets.timelineGroup.ITimelineGroup;

public abstract class TimelineGroupViewer<TIMELINE extends ITimeline> extends
		Viewer implements ITimelineGroupViewer<TIMELINE> {

	private ITimelineGroup<TIMELINE> timelineGroup;

	private ISelection selection = null;
	private ITimelineListener timelineListener = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
		}
	};

	public TimelineGroupViewer(ITimelineGroup<TIMELINE> timelineGroup) {
		this.timelineGroup = timelineGroup;
		this.timelineGroup.addTimelineListener(this.timelineListener);
	}

	@Override
	public Control getControl() {
		return (Control) this.timelineGroup;
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		this.selection = selection;
		fireSelectionChanged(new SelectionChangedEvent(this, selection));
	}

}
