package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.viewer.jointjs.JointJSContentProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSLabelProvider;
import com.bkahlert.nebula.viewer.jointjs.JointJSViewer;
import com.bkahlert.nebula.widgets.jointjs.JointJS;

@Demo
public class JointJSViewerDemo extends AbstractDemo {

	private static final Object NODE1 = new Object() {
		@Override
		public String toString() {
			return "node1";
		};
	};
	private static final Object NODE2 = new Object() {
		@Override
		public String toString() {
			return "node2";
		};
	};
	private static final Object NODE3 = new Object() {
		@Override
		public String toString() {
			return "node3";
		};
	};

	private JointJS jointjs;
	private JointJSViewer jointjsViewer;

	private static class MyLabelProvider extends LabelProvider implements
			JointJSLabelProvider {
		@Override
		public String getText(Object element) {
			if (element == NODE1) {
				return "node1";
			}
			if (element == NODE2) {
				return "node2";
			}
			if (element == NODE3) {
				return "node3";
			}
			return element.toString();
		}

		@Override
		public String getContent(Object element) {
			return "<ul><li>" + element.hashCode() + "</li><li>"
					+ element.toString() + "</li></ul>";
		}

		@Override
		public RGB getColor(Object element) {
			return ColorUtils.getRandomRGB();
		}

		@Override
		public RGB getBackgroundColor(Object element) {
			return ColorUtils.getRandomRGB();
		}

		@Override
		public RGB getBorderColor(Object element) {
			return ColorUtils.getRandomRGB();
		}

		@Override
		public Point getSize(Object element) {
			return null;
		}
	}

	private String json = null;

	@Override
	public void createControls(Composite composite) {
		Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("loading");
						try {
							JointJSViewerDemo.this.jointjs
									.load(JointJSViewerDemo.this.json);
						} catch (Exception e) {
							log(e.toString());
						}
						log("loaded");
					}
				}).start();
			}
		});

		Button saveButton = new Button(composite, SWT.PUSH);
		saveButton.setText("save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("loading");
						try {
							JointJSViewerDemo.this.json = JointJSViewerDemo.this.jointjs
									.save().get();
						} catch (Exception e) {
							log(e.toString());
						}
						log("loaded");
					}
				}).start();
			}
		});

		Button getNodesLinksButton = new Button(composite, SWT.PUSH);
		getNodesLinksButton.setText("log nodes/links");
		getNodesLinksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("getting nodes and links");
						try {
							log("Nodes: "
									+ StringUtils.join(
											JointJSViewerDemo.this.jointjs
													.getNodes().get(), ", "));
							log("Links: "
									+ StringUtils.join(
											JointJSViewerDemo.this.jointjs
													.getLinks().get(), ", "));
						} catch (Exception e) {
							log(e.toString());
						}
						log("got nodes and links");
					}
				}).start();
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.BORDER, "node://", "link://");
		this.jointjsViewer = new JointJSViewer(this.jointjs,
				new JointJSContentProvider() {

					@SuppressWarnings("unused")
					private Viewer viewer = null;

					@Override
					public void inputChanged(Viewer viewer, Object oldInput,
							Object newInput) {
						if (viewer instanceof JointJSViewer) {
							this.viewer = viewer;
						}
					}

					@Override
					public void dispose() {
					}

					@Override
					public String getNodeId(Object node) {
						return node.toString();
					}

					@Override
					public String getLinkId(Object link) {
						return null;
					}

					@Override
					public Object[] getNodes() {
						return new Object[] { NODE1, NODE2, NODE3 };
					}

					@Override
					public Point getNodePosition(Object node) {
						return null;
					}

					@Override
					public Object[] getPermanentLinks() {
						return new Object[] {
								NODE1.toString() + "," + NODE2.toString(),
								NODE2.toString() + "," + NODE3.toString() };
					}

					@Override
					public Object[] getLinks() {
						return new Object[] { NODE3.toString() + ","
								+ NODE1.toString() };
					}

					@Override
					public String getLinkSourceId(Object link) {
						return link.toString().split(",")[0];
					}

					@Override
					public String getLinkTargetId(Object link) {
						return link.toString().split(",")[1];
					}

				}, new MyLabelProvider()) {
		};

		this.jointjsViewer.setInput(new Object());
		this.jointjsViewer.refresh();

		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {

				return null;
			}
		});

	}
}
