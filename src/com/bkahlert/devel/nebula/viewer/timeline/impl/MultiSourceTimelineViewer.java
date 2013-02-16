package com.bkahlert.devel.nebula.viewer.timeline.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.viewer.timeline.IMultiSourceTimelineViewer;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.IBandGroupProvider;
import com.bkahlert.devel.nebula.viewer.timeline.provider.complex.ITimelineProvider;
import com.bkahlert.devel.nebula.widgets.timeline.ITimeline;
import com.bkahlert.devel.nebula.widgets.timeline.model.ITimelineInput;

/**
 * This
 * 
 * @author bkahlert
 * 
 */
public class MultiSourceTimelineViewer<TIMELINE extends ITimeline> extends
		TimelineViewer<TIMELINE> implements
		IMultiSourceTimelineViewer<TIMELINE> {

	private Object input;

	private ITimelineProvider<TIMELINE> timelineProvider = null;

	public MultiSourceTimelineViewer(final TIMELINE timeline) {
		super(timeline);
		ExecutorUtil.syncExec(new Runnable() {
			@Override
			public void run() {
				timeline.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						notifyInputChanged(
								MultiSourceTimelineViewer.this.input, null);
					}
				});
			}
		});
	}

	@Override
	public void setInput(Object input) {
		if (this.input != input) {
			Object oldInput = this.input;
			Object newInput = input;
			this.input = input;
			notifyInputChanged(oldInput, newInput);
		}
	}

	@Override
	public Object getInput() {
		return this.input;
	}

	@Override
	public void setTimelineProvider(ITimelineProvider<TIMELINE> timelineProvider) {
		notifyInputChanged(this.input, null);
		this.timelineProvider = timelineProvider;
		notifyInputChanged(null, this.input);
	}

	protected void notifyInputChanged(Object oldInput, Object newInput) {
		if (this.timelineProvider != null) {
			for (IBandGroupProvider bandGroupProvider : this.timelineProvider
					.getBandGroupProviders()) {
				bandGroupProvider.getContentProvider().inputChanged(this,
						oldInput, newInput);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void refresh(IProgressMonitor monitor) {
		if (this.timelineProvider == null)
			return;

		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);

		TIMELINE timeline = (TIMELINE) getControl();
		ITimelineInput timelineInput = this.timelineProvider
				.generateTimelineInput(timeline, subMonitor.newChild(1));
		// TODO scroll position und decorators beibehalten
		timeline.show(timelineInput, 300, 300, subMonitor.newChild(1));
	}

}
