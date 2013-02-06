package com.bkahlert.devel.nebula.viewer.timeline.impl;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.viewer.timeline.ITimelineLabelProvider;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;

public abstract class TimelineViewer extends Viewer implements ITimelineViewer {

	private ITimeline timeline;
	private ITimelineLabelProvider timelineLabelProvider;

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

	public TimelineViewer(ITimeline timeline) {
		this.timeline = timeline;
		this.timeline.addTimelineListener(this.timelineListener);
	}

	@Override
	public void setTimelineLabelProvider(
			ITimelineLabelProvider timelineLabelProvider) {
		this.timelineLabelProvider = timelineLabelProvider;
	}

	@Override
	public ITimelineLabelProvider getTimelineLabelProvider() {
		return timelineLabelProvider;
	}

	@Override
	public Control getControl() {
		return (Control) this.timeline;
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
