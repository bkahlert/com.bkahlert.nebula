package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ShellUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.jointjs.JointJS;
import com.bkahlert.nebula.widgets.jointjs.JointJS.IJointJSListener;
import com.bkahlert.nebula.widgets.jointjs.JointJSCell;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

@Demo
public class JointJSDemo extends AbstractDemo {

	private JointJS jointjs;

	private String json = null;
	protected Point pan = null;
	protected Double zoom = null;

	private List<String> idsWithCustomClass;

	@Override
	public void createControls(Composite composite) {
		Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(
						() -> {
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
							} catch (Exception e1) {
								log(e1.toString());
							}
							log("loaded");
						}).start();
			}
		});

		Button saveButton = new Button(composite, SWT.PUSH);
		saveButton.setText("save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("saving");
					try {
						JointJSDemo.this.json = JointJSDemo.this.jointjs.save()
								.get();

						JointJSDemo.this.pan = JointJSDemo.this.jointjs
								.getPan().get();
						log("pan: " + JointJSDemo.this.pan.x + ", "
								+ JointJSDemo.this.pan.y);

						JointJSDemo.this.zoom = JointJSDemo.this.jointjs
								.getZoom().get();
						log("zoom: " + JointJSDemo.this.zoom);
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("saved");
				}).start();
			}
		});

		Button getElementsLinksButton = new Button(composite, SWT.PUSH);
		getElementsLinksButton.setText("log elements/links");
		getElementsLinksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("getting elements and links");
					try {
						log("Elements: "
								+ StringUtils.join(JointJSDemo.this.jointjs
										.getElements().get(), ", "));
						log("Links: "
								+ StringUtils.join(JointJSDemo.this.jointjs
										.getLinks().get(), ", "));
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("got elements and links");
				}).start();
			}
		});

		Button setTitleButton = new Button(composite, SWT.PUSH);
		setTitleButton.setText("setTitle(\"Test\")");
		setTitleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("setting title");
					try {
						JointJSDemo.this.jointjs.setTitle("Test").get();
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("set title");
				}).start();
			}
		});

		Button getTitleButton = new Button(composite, SWT.PUSH);
		getTitleButton.setText("getTitle()");
		getTitleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("getting title");
					try {
						log(JointJSDemo.this.jointjs.getTitle().get());
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("got title");
				}).start();
			}
		});

		Button getPanButton = new Button(composite, SWT.PUSH);
		getPanButton.setText("getPan()");
		getPanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(
						() -> {
							log("getting pan");
							try {
								log(JointJSDemo.this.jointjs.getPan().get()
										.toString());
							} catch (Exception e1) {
								log(e1.toString());
							}
							log("got pan");
						}).start();
			}
		});

		Button enableButton = new Button(composite, SWT.PUSH);
		enableButton.setText("enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("enabling");
					try {
						JointJSDemo.this.jointjs.setEnabled(true);
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("enabled");
				}).start();
			}
		});

		Button disableButton = new Button(composite, SWT.PUSH);
		disableButton.setText("disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("diabling");
					try {
						JointJSDemo.this.jointjs.setEnabled(false);
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("disabled");
				}).start();
			}
		});

		this.createControlButton("Add Custom Class", () -> {
			log("add class debugCustomClass");
			try {
				JointJSDemo.this.jointjs
						.addCustomClass(JointJSDemo.this.idsWithCustomClass,
								"debugCustomClass");
			} catch (Exception e) {
				log(e.toString());
			}
			log("added class debugCustomClass");
		});

		this.createControlButton("Remove Custom Class", () -> {
			log("remove class debugCustomClass");
			try {
				JointJSDemo.this.jointjs
						.removeCustomClass(JointJSDemo.this.idsWithCustomClass,
								"debugCustomClass");
			} catch (Exception e) {
				log(e.toString());
			}
			log("removed class debugCustomClass");
		});

		this.createControlButton("Auto Layout", () -> {
			log("auto layouting");
			try {
				JointJSDemo.this.jointjs.autoLayout().get();
			} catch (Exception e) {
				log(e.toString());
			}
			log("auto layouted");
		});

		this.createControlButton("Fit On Screen", () -> {
			log("fitting on screen");
			try {
				JointJSDemo.this.jointjs.fitOnScreen().get();
			} catch (Exception e) {
				log(e.toString());
			}
			log("fitted on screen");
		});

		this.createControlButton("Shift By (20,20)", () -> {
			log("shifting by 20,20");
			try {
				JointJSDemo.this.jointjs.shiftBy(20, 20).get();
			} catch (Exception e) {
				log(e.toString());
			}
			log("shifted by 20,20");
		});

		this.createControlButton("Get Bounding Box",
				() -> {
					log("getting bounding box");
					try {
						log(JointJSDemo.this.jointjs.getBoundingBox().get()
								.toString());
					} catch (Exception e) {
						log(e.toString());
					}
					log("got bounding box");
				});

		this.createControlButton("Set Data", () -> {
			log("setting data");
			try {
				JointJSDemo.this.jointjs.setCustomData(
						"test",
						"crazy string for test reasons: "
								+ RandomStringUtils.random(32)
								+ " ← crazy, isn't it?!");
			} catch (Exception e) {
				log(e.toString());
			}
			log("set data");
		});

		this.createControlButton("Get Data", () -> {
			log("getting data");
			try {
				log(JointJSDemo.this.jointjs.getCustomData("test").get());
			} catch (Exception e) {
				log(e.toString());
			}
			log("got data");
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.jointjs = new JointJS(parent, SWT.BORDER, "element://", "link://");
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
			public void loaded(JointJSModel model) {
				log("loaded  " + model);
			}

			@Override
			public void save(JointJSModel model) {
				log("save " + model + "\n\t" + model.serialize());
			}

			@Override
			public void modified(JointJSModel model) {
				log("modified " + model);
			}

			@Override
			public void linkTitleChanged(String id, String title) {
				log("link title changed: " + id + " = " + title);
			}

			@Override
			public void hovered(JointJSCell cell, boolean hoveredIn) {
				log("hovered " + (hoveredIn ? "in" : "out") + ": " + cell);
			}

			@Override
			public void clicked(JointJSCell cell) {
				log("clicked " + cell);
			}

			@Override
			public void doubleClicked(JointJSCell cell) {
				log("double clicked " + cell);
			}
		});

		ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			String element1 = JointJSDemo.this.jointjs.createElement(
					"apiua://test3", "Hello Java", "bla<b> b</b>la",
					new Point(150, 300), new Point(200, 100)).get();

			String element2 = JointJSDemo.this.jointjs.createElement(
					"apiua://test4", "Hello Java", "bla bla",
					new Point(50, 30), new Point(120, 80)).get();

			String element3 = JointJSDemo.this.jointjs.createElement(
					"apiua://test40", "Hello Java", "bla bla",
					new Point(50, 30), new Point(220, 180)).get();

			String link1 = JointJSDemo.this.jointjs.createLink(null, element1,
					element2).get();

			JointJSDemo.this.jointjs.setLinkTitle(link1, "dssdööl sdldslkö ")
					.get();

			String link2 = JointJSDemo.this.jointjs.createPermanentLink(null,
					element2, element3).get();

			JointJSDemo.this.jointjs.setLinkTitle(link2, "perm link ").get();

			JointJSDemo.this.jointjs.setColor("apiua://test3", new RGB(255, 0,
					0));
			JointJSDemo.this.jointjs.setBackgroundColor("apiua://test3",
					new RGB(255, 0, 255));
			JointJSDemo.this.jointjs.setBorderColor("apiua://test3", new RGB(
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
			log("zoomed to 5.0: " + JointJSDemo.this.jointjs.getZoom().get());

			Thread.sleep(1500);
			JointJSDemo.this.jointjs.setZoom(1.0).get();
			log("zoomed to 1.0: " + JointJSDemo.this.jointjs.getZoom().get());

			Thread.sleep(1500);
			JointJSDemo.this.jointjs.setPosition("apiua://test40", 300, 100);
			log("moved test40");

			JointJSDemo.this.idsWithCustomClass = Arrays.asList(element1,
					element2, link1);

			return null;
		});
	}

	@Test
	public void testLoadSaveSeveralTimes() throws Exception {
		DOMConfigurator
				.configure("/Users/bkahlert/development/reps/com.bkahlert.nebula/log4j.xml");
		ShellUtils.runInSeparateShell(500, 300, shell -> {
			final JointJS jointjs = new JointJS(shell, SWT.NONE, "element://",
					"link://");
			return ExecUtils.nonUIAsyncExec((Callable<String>) () -> {
				jointjs.createElement("myid", "Hello World!",
						"Lorem ipsum<br/>lorem ipsum", new Point(10, 10),
						new Point(100, 30)).get();
				String json = jointjs.save().get();

				// save the loaded string and check for equality
					for (int i = 0; i < 10; i++) {
						String loadedJson = jointjs.load(json).get();
						String savedJson = jointjs.save().get();
						Assert.assertEquals(json, loadedJson);
						Assert.assertEquals(json, savedJson);
						json = savedJson;
					}
					return json;
				});
		}, 1000);
	}
}
