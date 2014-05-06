package com.bkahlert.nebula.viewer.jointjs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;

import com.bkahlert.nebula.data.TreeNode;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.viewer.timeline.ITimelineGroupViewer;
import com.bkahlert.nebula.viewer.timeline.impl.TimelineGroupViewer;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.timeline.IBaseTimeline;
import com.bkahlert.nebula.widgets.timeline.ITimeline;
import com.bkahlert.nebula.widgets.timeline.model.IOptions;

/**
 * This class implements a minimal implementation of
 * {@link ITimelineGroupViewer}.
 * <p>
 * {@link ITimeline}s are economically used. They are only created if necessary.
 * No more needed {@link ITimeline}s are used to load another key if no more
 * needed.
 * 
 * @author bkahlert
 * 
 * @param <TIMELINEGROUP>
 * @param <TIMELINE>
 */
public class JointJSViewer extends AbstractJointJSViewer {

	private static final Logger LOGGER = Logger.getLogger(JointJSViewer.class);

	public static Object[] breakUpAndRemoveDuplicates(Object input) {
		return TimelineGroupViewer.breakUpAndRemoveDuplicates(input);
	}

	/**
	 * Restores {@link IOptions} from and old to a new {@link IOptions} in a way
	 * the on refreshing the {@link IBaseTimeline} receives its new data but
	 * keeps its viewport.
	 * 
	 * @param oldOptions
	 * @param newOptions
	 */
	public static void restoreOptions(IOptions oldOptions, IOptions newOptions) {
		if (oldOptions != null && newOptions != null) {
			newOptions.setCenterStart(oldOptions.getCenterStart());
			newOptions.setPermanentDecorators(oldOptions
					.getPermanentDecorators());
			newOptions.setHotZones(oldOptions.getHotZones());
		}
	}

	private final JointJS jointjs;

	private final JointJSContentProvider contentProvider;
	private final JointJSLabelProvider labelProvider;
	private Object input;

	public JointJSViewer(JointJS jointjs,
			JointJSContentProvider contentProvider,
			JointJSLabelProvider labelProvider) {
		super(jointjs);
		Assert.isNotNull(jointjs);
		this.jointjs = jointjs;
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
	}

	@Override
	public void setInput(final Object newInput) {
		if (this.input != newInput) {
			Object oldInput = this.input;
			this.input = newInput;
			this.contentProvider.inputChanged(this, oldInput, newInput);
		}
	}

	@Override
	public Object getInput() {
		return this.input;
	}

	@Override
	public Future<Void> refresh(final IProgressMonitor monitor) {
		return ExecUtils.nonUISyncExec(JointJSViewer.class, "Refresh",
				new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						// TODO: Mit Breitensuche durchlaufen
						List<Object> modelNodes = JointJSViewer.this.getAllElementInBfsOrder(Arrays
								.asList(JointJSViewer.this.contentProvider
										.getElements(JointJSViewer.this.input)));

						List<String> realNodes = JointJSViewer.this.jointjs
								.getNodes().get();

						int defaultX = 0;
						int defaultY = 0;
						for (Object modelNode : modelNodes) {
							String id = JointJSViewer.this.contentProvider
									.getId(modelNode);
							if (realNodes.contains(id)) {
								// update
								String title = JointJSViewer.this.labelProvider
										.getTitle(modelNode);
								String content = JointJSViewer.this.labelProvider
										.getContent(modelNode);
								JointJSViewer.this.jointjs.setNodeTitle(id,
										title);
								JointJSViewer.this.jointjs.setNodeContent(id,
										content);
							} else {
								// new
								String title = JointJSViewer.this.labelProvider
										.getTitle(modelNode);
								String content = JointJSViewer.this.labelProvider
										.getContent(modelNode);
								Point position = JointJSViewer.this.labelProvider
										.getPosition(modelNode);
								Point size = JointJSViewer.this.labelProvider
										.getSize(modelNode);

								if (position == null) {
									position = new Point(defaultX, defaultY);
									defaultY += 15;
								}

								JointJSViewer.this.jointjs.createNode(id,
										title, content, position, size);
							}

							JointJSViewer.this.jointjs.setColor(id,
									JointJSViewer.this.labelProvider
											.getColor(modelNode));
							JointJSViewer.this.jointjs.setBackgroundColor(id,
									JointJSViewer.this.labelProvider
											.getBackgroundColor(modelNode));
							JointJSViewer.this.jointjs.setBorderColor(id,
									JointJSViewer.this.labelProvider
											.getBorderColor(modelNode));
						}

						return ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
							}
						}).get();
					}
				});
	}

	private List<Object> getAllElementInBfsOrder(List<Object> x) {
		TreeNode<Object> root = new TreeNode<Object>();
		this.getAllElementInBfsOrder(x, root);
		List<Object> all = new ArrayList<Object>();
		for (Iterator<Object> iterator = root.bfs(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object != null) {
				all.add(object);
			}
		}
		return all;
	}

	private void getAllElementInBfsOrder(List<Object> elements,
			TreeNode<Object> parent) {
		for (Object element : elements) {
			TreeNode<Object> child = new TreeNode<Object>(element);
			parent.add(child);
			Object[] children = this.contentProvider.getChildren(element);
			if (children != null) {
				this.getAllElementInBfsOrder(Arrays.asList(children), child);
			}
		}
	}
}
