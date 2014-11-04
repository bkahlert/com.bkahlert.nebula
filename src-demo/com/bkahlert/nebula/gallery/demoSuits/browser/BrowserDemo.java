package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.exception.JavaScriptException;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;
import com.bkahlert.nebula.widgets.browser.listener.IMouseListener;
import com.bkahlert.nebula.widgets.browser.runner.BrowserScriptRunner.JavaScriptExceptionListener;
import com.bkahlert.nebula.widgets.decoration.EmptyText;

@Demo
public class BrowserDemo extends AbstractDemo {

	private Browser browser;
	private String alertString = "Hello World!";
	private static String timeoutString = "15000";

	@Override
	public void createControls(Composite composite) {
		this.createControlButton("alert", new Runnable() {
			@Override
			public void run() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting");
						try {
							BrowserDemo.this.browser.run(
									"alert(\"" + BrowserDemo.this.alertString
											+ "\");").get();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log("alerted");
					}
				}).start();
			}
		});

		this.createControlButton("alert using external file", new Runnable() {
			@Override
			public void run() {
				new Thread(new Runnable() {
					@Override
					public void run() {
						log("alerting using external file");
						try {
							File jsFile = File.createTempFile(
									BrowserDemo.class.getSimpleName(), ".js");
							FileUtils.write(jsFile, "alert(\""
									+ BrowserDemo.this.alertString + "\");");
							BrowserDemo.this.browser.run(jsFile);
						} catch (Exception e) {
							log(e.toString());
						}
						log("alerted using external file");
					}
				}).start();
			}
		});

		Text text = new Text(composite, SWT.BORDER);
		text.setText(this.alertString);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				BrowserDemo.this.alertString = ((Text) e.getSource()).getText();
			}
		});

		Text timeout = new Text(composite, SWT.BORDER);
		timeout.setText(BrowserDemo.timeoutString);
		timeout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				BrowserDemo.timeoutString = ((Text) e.getSource()).getText();
			}
		});

		new EmptyText(timeout, "Timeout for page load");

		this.createControlButton("change background color using CSS injection",
				new Runnable() {
					@Override
					public void run() {
						new Thread(new Runnable() {
							@Override
							public void run() {
								log("changing background");
								try {
									BrowserDemo.this.browser
											.injectCss("html, body { background-color: "
													+ ColorUtils.getRandomRGB()
															.toHexString()
													+ "; }");
								} catch (Exception e) {
									log(e.toString());
								}
								log("changed background");
							}
						}).start();
					}
				});

		this.createControlButton("add focus border", new Runnable() {
			@Override
			public void run() {
				BrowserDemo.this.browser.addFocusBorder();
			}
		});

		this.createControlButton("remove focus border", new Runnable() {
			@Override
			public void run() {
				BrowserDemo.this.browser.removeFocusBorder();
			}
		});

		new Label(composite, SWT.NONE).setText("Exception Handling:");

		this.createControlButton("raise runtime exception", new Runnable() {
			@Override
			public void run() {
				try {
					BrowserDemo.this.browser.run("alert(x);").get();
				} catch (Exception e) {
					log(e);
				}
			}
		});

		this.createControlButton("raise syntax exception", new Runnable() {
			@Override
			public void run() {
				try {
					BrowserDemo.this.browser.run("alert('x);").get();
				} catch (Exception e) {
					log(e);
				}
			}
		});

		this.createControlButton("raise asynchronous runtime exception",
				new Runnable() {
					@Override
					public void run() {
						final JavaScriptExceptionListener javaScriptExceptionListener = new JavaScriptExceptionListener() {
							@Override
							public void thrown(
									JavaScriptException javaScriptException) {
								log(javaScriptException);
							}
						};
						BrowserDemo.this.browser
								.addJavaScriptExceptionListener(javaScriptExceptionListener);
						try {
							BrowserDemo.this.browser
									.run("window.setTimeout(function() { alert(x); }, 50);")
									.get();
							ExecUtils.nonUIAsyncExec(new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									BrowserDemo.this.browser
											.removeJavaScriptExceptionListener(javaScriptExceptionListener);
									return null;
								}
							}, 100);
						} catch (Exception e) {
							log("IMPLEMENTATION ERROR - This exception should have be thrown asynchronously!");
							log(e);
						}

					}
				});

	}

	@Override
	public void createDemo(Composite parent) {
		this.browser = new Browser(parent, SWT.BORDER);
		this.browser.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				log("hovered " + (entered ? "over" : "out") + " " + anker);
			}

			@Override
			public void ankerClicked(IAnker anker) {
				log("clicked on " + anker);
			}
		});
		this.browser.addFocusListener(new IFocusListener() {
			@Override
			public void focusLost(IElement element) {
				log("focus lost " + element);
			}

			@Override
			public void focusGained(IElement element) {
				log("focus gained " + element);
			}
		});
		this.browser.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				log("relative mouse pos " + e.x + "," + e.y);
			}
		});
		this.browser.addMouseListener(new IMouseListener() {
			@Override
			public void mouseMove(double x, double y) {
				log("absolute mouse pos " + x + "," + y);
			}

			@Override
			public void mouseDown(double x, double y, IElement element) {
				log("mouse down " + x + "," + y + " - " + element);
			}

			@Override
			public void mouseUp(double x, double y, IElement element) {
				log("mouse up " + x + "," + y + " - " + element);
			}

			@Override
			public void clicked(double x, double y, IElement element) {
				log("clicked " + x + "," + y + " - " + element);
			}
		});

		final Future<Boolean> success = this.browser.open(
				"http://wikipedia.com",
				Integer.parseInt(BrowserDemo.timeoutString));
		ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					if (success.get()) {
						log("Page loaded successfully");
					} else {
						log("Page load timed out");
					}
				} catch (Exception e) {
					log(e.getMessage());
				}
				log(ExecUtils.syncExec(new Callable<String>() {
					@Override
					public String call() throws Exception {
						return BrowserDemo.this.browser.getBrowser().getUrl();
					}
				}));
				return null;
			}
		});
	}
}
