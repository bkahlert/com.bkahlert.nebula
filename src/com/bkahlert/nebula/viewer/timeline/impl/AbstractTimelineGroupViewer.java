package com.bkahlert.nebula.viewer.timeline.impl;

import java.util.concurrent.Future;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.ITimelineListener;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineEvent;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.widgets.timelinegroup.impl.TimelineGroup;

/**
 * This abstract {@link ITimelineGroupViewer} implements the
 * {@link ISelectionPolicy} functionality.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINEGROUP>
 * @param <INPUT>
 */
public abstract class AbstractTimelineGroupViewer<TIMELINEGROUP extends TimelineGroup<TIMELINE, INPUT>, TIMELINE extends ITimeline, INPUT>
		extends Viewer implements
		ITimelineGroupViewer<TIMELINEGROUP, TIMELINE, INPUT> {

	private final TIMELINEGROUP timelineGroup;

	private ISelection selection = null;
	private final ITimelineListener timelineListener = new ITimelineListener() {
		@Override
		public void clicked(TimelineEvent event) {
			AbstractTimelineGroupViewer.this
					.setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void middleClicked(TimelineEvent event) {
			AbstractTimelineGroupViewer.this
					.setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void rightClicked(TimelineEvent event) {
			AbstractTimelineGroupViewer.this
					.setSelection(new StructuredSelection(event.getSource()));
		}

		@Override
		public void doubleClicked(TimelineEvent event) {
			AbstractTimelineGroupViewer.this
					.setSelection(new StructuredSelection(event.getSource()));
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

	public AbstractTimelineGroupViewer(TIMELINEGROUP timelineGroup) {
		Assert.isNotNull(timelineGroup);
		this.timelineGroup = timelineGroup;
		this.timelineGroup.addTimelineListener(this.timelineListener);
		Runnable addDisposeListener = new Runnable() {
			@Override
			public void run() {
				AbstractTimelineGroupViewer.this.timelineGroup
						.addDisposeListener(new DisposeListener() {
							@Override
							public void widgetDisposed(DisposeEvent e) {
								if (AbstractTimelineGroupViewer.this.timelineGroup != null
										&& !AbstractTimelineGroupViewer.this.timelineGroup
												.isDisposed()) {
									AbstractTimelineGroupViewer.this.timelineGroup
											.dispose();
								}
							}
						});
			}
		};
		if (Display.getCurrent() == Display.getDefault()) {
			addDisposeListener.run();
		} else {
			Display.getDefault().syncExec(addDisposeListener);
		}
	}

	@Override
	public TIMELINEGROUP getControl() {
		return this.timelineGroup;
	}

	@Override
	public ISelection getSelection() {
		return this.selection;
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		this.selection = selection;
		this.fireSelectionChanged(new SelectionChangedEvent(this, selection));
	}

	@Override
	public abstract Future<Void> refresh(IProgressMonitor monitor);

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
