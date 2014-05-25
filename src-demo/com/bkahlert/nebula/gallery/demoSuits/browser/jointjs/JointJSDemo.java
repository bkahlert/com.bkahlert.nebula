package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ExecUtils.ParametrizedCallable;
import com.bkahlert.nebula.utils.ShellUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.IJointJSListener;

@Demo
public class JointJSDemo extends AbstractDemo {

	private JointJS jointjs;

	private String json = null;
	protected Point pan = null;
	protected Double zoom = null;

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
							JointJSDemo.this.jointjs
									.load(JointJSDemo.this.json);
							JointJSDemo.this.jointjs
									.setZoom(JointJSDemo.this.zoom);
							if (JointJSDemo.this.pan != null) {
								JointJSDemo.this.jointjs.setPan(
										JointJSDemo.this.pan.x,
										JointJSDemo.this.pan.y);
							}
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
						log("saving");
						try {
							JointJSDemo.this.json = JointJSDemo.this.jointjs
									.save().get();

							JointJSDemo.this.pan = JointJSDemo.this.jointjs
									.getPan().get();
							log("pan: " + JointJSDemo.this.pan.x + ", "
									+ JointJSDemo.this.pan.y);

							JointJSDemo.this.zoom = JointJSDemo.this.jointjs
									.getZoom().get();
							log("zoom: " + JointJSDemo.this.zoom);
						} catch (Exception e) {
							log(e.toString());
						}
						log("saved");
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
									+ StringUtils.join(JointJSDemo.this.jointjs
											.getNodes().get(), ", "));
							log("Links: "
									+ StringUtils.join(JointJSDemo.this.jointjs
											.getLinks().get(), ", "));
						} catch (Exception e) {
							log(e.toString());
						}
						log("got nodes and links");
					}
				}).start();
			}
		});

		Button setTitleButton = new Button(composite, SWT.PUSH);
		setTitleButton.setText("setTitle(\"Test\")");
		setTitleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("setting title");
						try {
							JointJSDemo.this.jointjs.setTitle("Test").get();
						} catch (Exception e) {
							log(e.toString());
						}
						log("set title");
					}
				}).start();
			}
		});

		Button getTitleButton = new Button(composite, SWT.PUSH);
		getTitleButton.setText("getTitle()");
		getTitleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("getting title");
						try {
							log(JointJSDemo.this.jointjs.getTitle().get());
						} catch (Exception e) {
							log(e.toString());
						}
						log("got title");
					}
				}).start();
			}
		});

		Button getPanButton = new Button(composite, SWT.PUSH);
		getPanButton.setText("getPan()");
		getPanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("getting pan");
						try {
							log(JointJSDemo.this.jointjs.getPan().get()
									.toString());
						} catch (Exception e) {
							log(e.toString());
						}
						log("got pan");
					}
				}).start();
			}
		});

		Button enableButton = new Button(composite, SWT.PUSH);
		enableButton.setText("enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("enabling");
						try {
							JointJSDemo.this.jointjs.setEnabled(true);
						} catch (Exception e) {
							log(e.toString());
						}
						log("enabled");
					}
				}).start();
			}
		});

		Button disableButton = new Button(composite, SWT.PUSH);
		disableButton.setText("disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("diabling");
						try {
							JointJSDemo.this.jointjs.setEnabled(false);
						} catch (Exception e) {
							log(e.toString());
						}
						log("disabled");
					}
				}).start();
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.BORDER, "node://", "link://");
		this.jointjs.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + anker);
			}

			@Override
			public void ankerClicked(IAnker anker) {
				log("clicked on " + anker);
			}
		});
		this.jointjs.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				log("focus lost " + element);
			}

			@Override
			public void focusGained(IElement element) {
				log("focus gained " + element);
			}
		});
		this.jointjs.addJointJSListener(new IJointJSListener() {
			@Override
			public void loaded(String json) {
				log("loaded  " + json);
			}

			@Override
			public void save(String json) {
				log("save " + json);
			}

			@Override
			public void linkTitleChanged(String id, String title) {
				log("link title changed: " + id + " = " + title);
			}

			@Override
			public void hovered(String id, boolean hoveredIn) {
				log("hovered " + (hoveredIn ? "in" : "out") + ": " + id);
			}
		});

		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				String node1 = JointJSDemo.this.jointjs.createNode(
						"sua://test3", "Hello Java", "bla<b> b</b>la",
						new Point(150, 300), new Point(200, 100)).get();

				String node2 = JointJSDemo.this.jointjs.createNode(
						"sua://test4", "Hello Java", "bla bla",
						new Point(50, 30), new Point(120, 80)).get();

				String node3 = JointJSDemo.this.jointjs.createNode(
						"sua://test40", "Hello Java", "bla bla",
						new Point(50, 30), new Point(220, 180)).get();

				String link1 = JointJSDemo.this.jointjs.createLink(null, node1,
						node2).get();

				JointJSDemo.this.jointjs.setLinkTitle(link1,
						"dssdööl sdldslkö ").get();

				String link2 = JointJSDemo.this.jointjs.createPermanentLink(
						null, node2, node3).get();

				JointJSDemo.this.jointjs.setLinkTitle(link2, "perm link ")
						.get();

				JointJSDemo.this.jointjs.setColor("sua://test3", new RGB(255,
						0, 0));
				JointJSDemo.this.jointjs.setBackgroundColor("sua://test3",
						new RGB(255, 0, 255));
				JointJSDemo.this.jointjs.setBorderColor("sua://test3", new RGB(
						255, 128, 0));

				log("initial zoom: " + JointJSDemo.this.jointjs.getZoom().get());

				Thread.sleep(1500);
				JointJSDemo.this.jointjs.zoomIn().get();
				log("zoomed in: " + JointJSDemo.this.jointjs.getZoom().get());

				Thread.sleep(1500);
				JointJSDemo.this.jointjs.zoomOut().get();
				log("zoomed out: " + JointJSDemo.this.jointjs.getZoom().get());
				JointJSDemo.this.jointjs.zoomOut().get();
				log("zoomed out: " + JointJSDemo.this.jointjs.getZoom().get());

				Thread.sleep(1500);
				JointJSDemo.this.jointjs.setZoom(5.0).get();
				log("zoomed to 5.0: "
						+ JointJSDemo.this.jointjs.getZoom().get());

				Thread.sleep(1500);
				JointJSDemo.this.jointjs.setZoom(1.0).get();
				log("zoomed to 1.0: "
						+ JointJSDemo.this.jointjs.getZoom().get());

				Thread.sleep(1500);
				JointJSDemo.this.jointjs.setPosition("sua://test40", 300, 100);
				log("moved test40");

				return null;
			}
		});
	}

	@Test
	public void testLoadSaveSeveralTimes() throws Exception {
		DOMConfigurator
				.configure("/Users/bkahlert/development/reps/com.bkahlert.devel.nebula/log4j.xml");
		ShellUtils.runInSeparateShell(500, 300,
				new ParametrizedCallable<Shell, Future<String>>() {
					@Override
					public Future<String> call(Shell shell) throws Exception {
						final JointJS jointjs = new JointJS(shell, SWT.NONE,
								"node://", "link://");
						return ExecUtils.nonUIAsyncExec(new Callable<String>() {
							@Override
							public String call() throws Exception {
								jointjs.createNode("myid", "Hello World!",
										"Lorem ipsum<br/>lorem ipsum",
										new Point(10, 10), new Point(100, 30))
										.get();
								String json = jointjs.save().get();

								// save the loaded string and check for equality
								for (int i = 0; i < 10; i++) {
									String loadedJson = jointjs.load(json)
											.get();
									String savedJson = jointjs.save().get();
									Assert.assertEquals(json, loadedJson);
									Assert.assertEquals(json, savedJson);
									json = savedJson;
								}
								return json;
							}
						});
					}
				}, 1000);
	}
}
