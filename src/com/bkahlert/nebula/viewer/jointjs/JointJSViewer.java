package com.bkahlert.nebula.viewer.jointjs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;

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

	private static class Cell {
		private final String id;

		public Cell(String id) {
			super();
			this.id = id;
		}

		public String getId() {
			return this.id;
		}
	}

	private static class Link extends Cell {
		private final String sourceId;
		private final String targetId;
		private final String[] labels;

		public Link(String id, String sourceId, String targetId, String[] labels) {
			super(id);
			this.sourceId = sourceId;
			this.targetId = targetId;
			this.labels = labels;
		}

		@Override
		public String toString() {
			return "Link (Labels = " + StringUtils.join(this.labels, ", ")
					+ "): " + this.sourceId + " -> " + this.targetId;
		}
	}

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
					private List<String> existantNodeIds = null;
					private final int defaultX = 0;
					private int defaultY = 0;

					/**
					 * Creates the given node if necessary and updates all
					 * information in the diagram. All links reflecting the
					 * hierarchy are also rendered.
					 * 
					 * @param node
					 * @param parentNode
					 * @param linksToBeCreated
					 *            is used to collect all links that must be
					 *            created after all the hierarchy was rendered.
					 */
					private void syncNodesAndInheritance(Object node,
							Object parentNode, List<Link> linksToBeCreated) {
						String id = JointJSViewer.this.contentProvider
								.getId(node);

						if (this.existantNodeIds.contains(id)) {
							// update
							String title = JointJSViewer.this.labelProvider
									.getTitle(node);
							String content = JointJSViewer.this.labelProvider
									.getContent(node);
							JointJSViewer.this.jointjs.setNodeTitle(id, title);
							JointJSViewer.this.jointjs.setNodeContent(id,
									content);
						} else {
							// new
							String title = JointJSViewer.this.labelProvider
									.getTitle(node);
							String content = JointJSViewer.this.labelProvider
									.getContent(node);
							Point position = JointJSViewer.this.labelProvider
									.getPosition(node);
							Point size = JointJSViewer.this.labelProvider
									.getSize(node);

							if (position == null) {
								position = new Point(this.defaultX,
										this.defaultY);
								this.defaultY += 40;
							}

							try {
								JointJSViewer.this.jointjs.createNode(id,
										title, content, position, size).get();
								this.existantNodeIds.add(id);
							} catch (Exception e) {
								LOGGER.error("Error creating node " + id, e);
							}
						}

						JointJSViewer.this.jointjs
								.setColor(id, JointJSViewer.this.labelProvider
										.getColor(node));
						JointJSViewer.this.jointjs.setBackgroundColor(id,
								JointJSViewer.this.labelProvider
										.getBackgroundColor(node));
						JointJSViewer.this.jointjs.setBorderColor(id,
								JointJSViewer.this.labelProvider
										.getBorderColor(node));

						this.createPermanentLink(parentNode, node);

						Object[] links = JointJSViewer.this.contentProvider
								.getLinks(node);
						if (links == null) {
							links = new Object[0];
						}
						for (Object link : links) {
							String targetId = JointJSViewer.this.contentProvider
									.getId(link);
							String title = JointJSViewer.this.labelProvider
									.getText(link);
							String[] labels = title != null ? new String[] { title }
									: new String[0];
							linksToBeCreated.add(new Link(null, id, targetId,
									labels));
						}

						Object[] children = JointJSViewer.this.contentProvider
								.getChildren(node);
						if (children == null) {
							children = new Object[0];
						}
						for (Object child : children) {
							this.syncNodesAndInheritance(child, node,
									linksToBeCreated);
						}
					}

					private void createPermanentLink(Object parentNode,
							Object node) {
						if (parentNode == null || node == null) {
							return;
						}
						String sourceId = JointJSViewer.this.contentProvider
								.getId(node);
						String targetId = JointJSViewer.this.contentProvider
								.getId(parentNode);
						String id = null;
						JointJSViewer.this.jointjs.createPermanentLink(id,
								sourceId, targetId);
					}

					@Override
					public Void call() throws Exception {
						this.existantNodeIds = JointJSViewer.this.jointjs
								.getNodes().get();

						Object[] topLevelNodes = JointJSViewer.this.contentProvider
								.getElements(JointJSViewer.this.input);

						List<Link> linksToBeCreated = new ArrayList<Link>();
						for (Object topLevelNode : topLevelNodes) {
							this.syncNodesAndInheritance(topLevelNode, null,
									linksToBeCreated);
						}

						for (Link link : linksToBeCreated) {
							String linkId = JointJSViewer.this.jointjs
									.createLink(link.getId(), link.sourceId,
											link.targetId).get();
							for (int i = 0; link.labels != null
									&& i < link.labels.length; i++) {
								JointJSViewer.this.jointjs.setText(linkId, i,
										link.labels[i]);
							}
						}

						return ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
							}
						}).get();
					}
				});
	}

}
