package com.bkahlert.devel.nebula.viewer.timeline.impl;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.ITimelineViewer;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;

public abstract class TimelineViewer<TIMELINE extends ITimeline> extends Viewer
		implements ITimelineViewer {

	private TIMELINE timeline;

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
		public void resizeStarted(TimelineEvent event) {
		}

		@Override
		public void resizing(TimelineEvent event) {
		}

		@Override
		public void resized(TimelineEvent event) {
		}

		@Override
		public void hoveredIn(TimelineEvent event) {
		}

		@Override
		public void hoveredOut(TimelineEvent event) {
		}
	};

	public TimelineViewer(TIMELINE timeline) {
		this.timeline = timeline;
		this.timeline.addTimelineListener(this.timelineListener);
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				TimelineViewer.this.timeline
						.addDisposeListener(new DisposeListener() {
							@Override
							public void widgetDisposed(DisposeEvent e) {
								if (TimelineViewer.this.timeline != null
										&& !TimelineViewer.this.timeline
												.isDisposed()) {
									TimelineViewer.this.timeline.dispose();
								}
							}
						});
			}
		});
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

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
