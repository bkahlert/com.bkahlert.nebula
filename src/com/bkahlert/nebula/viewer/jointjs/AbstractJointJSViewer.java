package com.bkahlert.nebula.viewer.jointjs;

import java.util.concurrent.Future;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

/**
 * This abstract {@link ITimelineGroupViewer} implements the
 * {@link ISelectionPolicy} functionality.
 * 
 * @author bkahlert
 * 
 * @param <INPUT>
 */
public abstract class AbstractJointJSViewer extends Viewer {

	private final JointJS jointjs;

	private ISelection selection = null;
	private final JointJS.IJointJSListener jointJsListener = new JointJS.IJointJSListener() {

		@Override
		public void loaded(String json) {
			// TODO Auto-generated method stub

		}

		@Override
		public void save(String json) {
			// TODO Auto-generated method stub

		}

		@Override
		public void linkTitleChanged(String id, String title) {
			// TODO Auto-generated method stub

		}

		// @Override
		// public void selected(TimelineEvent event) {
		// AbstractJointJSViewer.this
		// .setSelection(new StructuredSelection(event.getSources()));
		// };
	};

	public AbstractJointJSViewer(JointJS jointjs) {
		Assert.isNotNull(jointjs);
		this.jointjs = jointjs;
		this.jointjs.addJointJSListener(this.jointJsListener);
		Runnable addDisposeListener = new Runnable() {
			@Override
			public void run() {
				AbstractJointJSViewer.this.jointjs
						.addDisposeListener(new DisposeListener() {
							@Override
							public void widgetDisposed(DisposeEvent e) {
								if (AbstractJointJSViewer.this.jointjs != null
										&& !AbstractJointJSViewer.this.jointjs
												.isDisposed()) {
									AbstractJointJSViewer.this.jointjs
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
	public JointJS getControl() {
		return this.jointjs;
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

	public abstract Future<Void> refresh(IProgressMonitor monitor);

	@Override
	public void refresh() {
		this.refresh(null);
	}

}
