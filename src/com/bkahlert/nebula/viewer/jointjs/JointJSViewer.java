package com.bkahlert.nebula.viewer.jointjs;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

					@Override
					public Void call() throws Exception {
						this.existantNodeIds = JointJSViewer.this.jointjs
								.getNodes().get();

						Object[] nodes = JointJSViewer.this.contentProvider
								.getNodes();
						for (Object node : nodes) {
							this.createNode(node);
						}

						Object[] permanentLinks = JointJSViewer.this.contentProvider
								.getPermanentLinks();
						for (Object permanentLink : permanentLinks) {
							this.createLink(permanentLink, true);
						}

						Object[] links = JointJSViewer.this.contentProvider
								.getLinks();
						for (Object link : links) {
							this.createLink(link, false);
						}

						return ExecUtils.asyncExec(new Runnable() {
							@Override
							public void run() {
							}
						}).get();
					}

					private void createNode(Object node) {
						String id = JointJSViewer.this.contentProvider
								.getNodeId(node);

						if (this.existantNodeIds.contains(id)) {
							// update
							String title = JointJSViewer.this.labelProvider
									.getText(node);
							String content = JointJSViewer.this.labelProvider
									.getContent(node);
							JointJSViewer.this.jointjs.setNodeTitle(id, title);
							JointJSViewer.this.jointjs.setNodeContent(id,
									content);
						} else {
							// new
							String title = JointJSViewer.this.labelProvider
									.getText(node);
							String content = JointJSViewer.this.labelProvider
									.getContent(node);
							Point position = JointJSViewer.this.contentProvider
									.getNodePosition(node);
							Point size = JointJSViewer.this.labelProvider
									.getSize(node);

							if (position == null) {
								position = new Point(this.defaultX,
										this.defaultY);
								this.defaultY += 40;
							}

							try {
								id = JointJSViewer.this.jointjs.createNode(id,
										title, content, position, size).get();
								this.existantNodeIds.add(id);
							} catch (Exception e) {
								LOGGER.error("Error creating node " + id, e);
							}
						}

						if (id == null) {
							LOGGER.error("ID missing for created/updated node!");
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
						Point size = JointJSViewer.this.labelProvider
								.getSize(node);
						if (size != null) {
							JointJSViewer.this.jointjs.setSize(id, size.x,
									size.y);
						}
					}

					private void createLink(Object link, boolean permanent)
							throws InterruptedException, ExecutionException {
						String id = JointJSViewer.this.contentProvider
								.getLinkId(link);
						String sourceId = JointJSViewer.this.contentProvider
								.getLinkSourceId(link);
						String targetId = JointJSViewer.this.contentProvider
								.getLinkTargetId(link);

						if (permanent) {
							id = JointJSViewer.this.jointjs
									.createPermanentLink(id, sourceId, targetId)
									.get();
						} else {
							id = JointJSViewer.this.jointjs.createLink(id,
									sourceId, targetId).get();
						}

						String[] texts = new String[0];
						String text = JointJSViewer.this.labelProvider
								.getText(link);
						if (text != null) {
							texts = new String[] { text };
						}
						for (int i = 0; texts != null && i < texts.length; i++) {
							JointJSViewer.this.jointjs.setText(id, i, texts[i]);
						}
					}
				});
	}

}
