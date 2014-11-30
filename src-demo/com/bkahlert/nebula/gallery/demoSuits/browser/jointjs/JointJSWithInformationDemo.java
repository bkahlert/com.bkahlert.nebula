package com.bkahlert.nebula.gallery.demoSuits.browser.jointjs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Test;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.demoSuits.information.InformationControlManagerDemo;
import com.bkahlert.nebula.information.EnhanceableInformationControl;
import com.bkahlert.nebula.information.EnhanceableInformationControl.Delegate;
import com.bkahlert.nebula.information.ISubjectInformationProvider;
import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlCreator;
import com.bkahlert.nebula.information.InformationControlManager;
import com.bkahlert.nebula.information.InformationControlManagerUtils;
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
import com.bkahlert.nebula.widgets.jointjs.JointJSLink;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

@Demo
public class JointJSWithInformationDemo extends AbstractDemo {

	private JointJS jointjs;

	private String json = null;
	protected Point pan = null;
	protected Double zoom = null;

	InformationControlCreator<URI> creator = new InformationControlCreator<URI>() {
		@Override
		protected InformationControl<URI> doCreateInformationControl(
				Shell parent) {
			return new EnhanceableInformationControl<URI, Delegate<URI>>(
					InformationControlManagerDemo.class.getClassLoader(),
					URI.class, parent, () -> new Delegate<URI>() {
						private Label label;

						@Override
						public Composite build(Composite parent) {
							parent.setLayout(GridLayoutFactory.fillDefaults()
									.create());
							this.label = new Label(parent, SWT.BORDER);
							this.label.setLayoutData(GridDataFactory
									.fillDefaults().grab(true, true).create());
							return parent;
						}

						@Override
						public boolean load(URI input,
								ToolBarManager toolBarManager) {
							if (input == null) {
								return false;
							}

							if (toolBarManager != null) {
								Action showCurrentManagerAction = new Action() {
									@Override
									public String getText() {
										return "Curr. Mngr";
									};

									@Override
									public void run() {
										log("Current manager: "
												+ InformationControlManagerUtils
														.getCurrentManager());
									};
								};
								Action showAgainAction = new Action() {
									@Override
									public String getText() {
										return "Show Again";
									};

									@Override
									public void run() {
										replaceInformationControlContent();
									};
								};
								toolBarManager.add(showCurrentManagerAction);
								toolBarManager.add(showAgainAction);
							}
							this.label.setText(input.toString());
							return true;
						}
					});
		}
	};

	public static void replaceInformationControlContent() {
		log("Replacing...");
		InformationControlManagerUtils
				.getCurrentManager(String.class)
				.setInformation(
						"New information...\nTimestamp: "
								+ new Date().getTime()
								+ "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nEND");
	}

	@Override
	public void createControls(Composite composite) {
		Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setText("load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("loading");
					try {
						JointJSWithInformationDemo.this.jointjs
								.load(JointJSWithInformationDemo.this.json);
						JointJSWithInformationDemo.this.jointjs
								.setZoom(JointJSWithInformationDemo.this.zoom);
						if (JointJSWithInformationDemo.this.pan != null) {
							JointJSWithInformationDemo.this.jointjs.setPan(
									JointJSWithInformationDemo.this.pan.x,
									JointJSWithInformationDemo.this.pan.y);
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
				new Thread(
						() -> {
							log("saving");
							try {
								JointJSWithInformationDemo.this.json = JointJSWithInformationDemo.this.jointjs
										.save().get();

								JointJSWithInformationDemo.this.pan = JointJSWithInformationDemo.this.jointjs
										.getPan().get();
								log("pan: "
										+ JointJSWithInformationDemo.this.pan.x
										+ ", "
										+ JointJSWithInformationDemo.this.pan.y);

								JointJSWithInformationDemo.this.zoom = JointJSWithInformationDemo.this.jointjs
										.getZoom().get();
								log("zoom: "
										+ JointJSWithInformationDemo.this.zoom);
							} catch (Exception e1) {
								log(e1.toString());
							}
							log("saved");
						}).start();
			}
		});

		Button getNodesLinksButton = new Button(composite, SWT.PUSH);
		getNodesLinksButton.setText("log elements/links");
		getNodesLinksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("getting elements and links");
					try {
						log("Nodes: "
								+ StringUtils.join(
										JointJSWithInformationDemo.this.jointjs
												.getElements().get(), ", "));
						log("Links: "
								+ StringUtils.join(
										JointJSWithInformationDemo.this.jointjs
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
						JointJSWithInformationDemo.this.jointjs
								.setTitle("Test").get();
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
						log(JointJSWithInformationDemo.this.jointjs.getTitle()
								.get());
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("got title");
				}).start();
			}
		});

		this.createControlButton(
				"setContent",
				() -> {
					log("settint content");
					try {
						for (String id : JointJSWithInformationDemo.this.jointjs
								.getElements().get()) {
							String content = "";
							for (int i = 0, m = (int) (Math.random() * 20); i < m; i++) {
								content += " "
										+ RandomStringUtils
												.randomAlphabetic((int) (Math
														.random() * 20));
							}
							JointJSWithInformationDemo.this.jointjs
									.setElementContent(id, content).get();
						}
					} catch (Exception e) {
						log(e.toString());
					}
					log("set content");
				});

		Button getPanButton = new Button(composite, SWT.PUSH);
		getPanButton.setText("getPan()");
		getPanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				new Thread(() -> {
					log("getting pan");
					try {
						log(JointJSWithInformationDemo.this.jointjs.getPan()
								.get().toString());
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
						JointJSWithInformationDemo.this.jointjs
								.setEnabled(true);
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
						JointJSWithInformationDemo.this.jointjs
								.setEnabled(false);
					} catch (Exception e1) {
						log(e1.toString());
					}
					log("disabled");
				}).start();
			}
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
				log("save " + model);
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
				log("hovered " + (hoveredIn ? "in" : "out") + ": "
						+ cell.getId());
			}
		});

		InformationControlManager<Composite, URI> informationManager = new InformationControlManager<Composite, URI>(
				URI.class, this.creator,
				new ISubjectInformationProvider<Composite, URI>() {
					private URI hovered = null;

					private final JointJS.IJointJSListener listener = new JointJS.JointJSListener() {
						@Override
						public void hovered(JointJSCell cell, boolean hoveredIn) {
							if (hoveredIn && cell != null
									&& !(cell instanceof JointJSLink)
									&& cell.getId().startsWith("apiua://")) {
								try {
									hovered = new URI(cell.getId());
								} catch (URISyntaxException e) {
									hovered = null;
								}
							} else {
								hovered = null;
							}
						}
					};

					@Override
					public void register(Composite subject) {
						JointJSWithInformationDemo.this.jointjs
								.addJointJSListener(this.listener);
					}

					@Override
					public void unregister(Composite subject) {
						JointJSWithInformationDemo.this.jointjs
								.removeJointJSListener(this.listener);
					}

					@Override
					public Point getHoverArea() {
						return new Point(1, 1);
					}

					@Override
					public URI getInformation() {
						return this.hovered;
					}
				});
		informationManager.install(this.jointjs);

		ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			String element1 = JointJSWithInformationDemo.this.jointjs
					.createElement("apiua://test3", "Hello Java",
							"bla<b> b</b>la", new Point(150, 300),
							new Point(200, 100)).get();

			String element2 = JointJSWithInformationDemo.this.jointjs
					.createElement("apiua://test4", "Hello Java", "bla bla",
							new Point(50, 30), new Point(120, 80)).get();

			String element3 = JointJSWithInformationDemo.this.jointjs
					.createElement("apiua://test40", "Hello Java", "bla bla",
							new Point(50, 30), new Point(220, 180)).get();

			String link1 = JointJSWithInformationDemo.this.jointjs.createLink(
					null, element1, element2).get();

			JointJSWithInformationDemo.this.jointjs.setLinkTitle(link1,
					"dssdööl sdldslkö ").get();

			String link2 = JointJSWithInformationDemo.this.jointjs
					.createPermanentLink(null, element2, element3).get();

			JointJSWithInformationDemo.this.jointjs.setLinkTitle(link2,
					"perm link ").get();

			JointJSWithInformationDemo.this.jointjs.setColor("apiua://test3",
					new RGB(255, 0, 0));
			JointJSWithInformationDemo.this.jointjs.setBackgroundColor(
					"apiua://test3", new RGB(255, 0, 255));
			JointJSWithInformationDemo.this.jointjs.setBorderColor(
					"apiua://test3", new RGB(255, 128, 0));

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
