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

	private static final Object NODE1 = new Object();
	private static final Object NODE2 = new Object();
	private static final Object NODE3 = new Object();

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
		public Point getPosition(Object element) {
			return null;
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
					public String getId(Object element) {
						if (element == NODE1) {
							return "node1";
						}
						if (element == NODE2) {
							return "node2";
						}
						if (element == NODE3) {
							return "node3";
						}
						return null;
					}

					@Override
					public boolean hasChildren(Object element) {
						return this.getChildren(element).length > 0;
					}

					@Override
					public Object getParent(Object element) {
						if (element == NODE3) {
							return NODE1;
						}
						return null;
					}

					@Override
					public Object[] getElements(Object inputElement) {
						return new Object[] { NODE1, NODE2 };
					}

					@Override
					public Object[] getChildren(Object parentElement) {
						if (parentElement == NODE1) {
							return new Object[] { NODE3 };
						}
						return new Object[0];
					}

					@Override
					public Object[] getLinks(Object element) {
						if (element == NODE1) {
							return new Object[] { NODE2 };
						}
						if (element == NODE2) {
							return new Object[] { NODE3 };
						}
						return new Object[0];
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
