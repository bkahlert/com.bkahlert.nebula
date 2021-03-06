package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
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
		this.createControlButton(
				"alert",
				() -> new Thread(() -> {
					log("alerting");
					try {
						BrowserDemo.this.browser.run(
								"alert(\"" + BrowserDemo.this.alertString
										+ "\");").get();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ExecutionException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					log("alerted");
				}).start());

		this.createControlButton(
				"alert using external file",
				() -> new Thread(() -> {
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
				}).start());

		Text text = new Text(composite, SWT.BORDER);
		text.setText(this.alertString);
		text.addModifyListener(e -> BrowserDemo.this.alertString = ((Text) e
				.getSource()).getText());

		Text timeout = new Text(composite, SWT.BORDER);
		timeout.setText(BrowserDemo.timeoutString);
		timeout.addModifyListener(e -> BrowserDemo.timeoutString = ((Text) e
				.getSource()).getText());

		new EmptyText(timeout, "Timeout for page load");

		this.createControlButton(
				"change background color using CSS injection",
				() -> new Thread(() -> {
					log("changing background");
					try {
						BrowserDemo.this.browser
								.injectCss("html, body { background-color: "
										+ ColorUtils.getRandomRGB()
												.toDecString() + "; }");
					} catch (Exception e) {
						log(e.toString());
					}
					log("changed background");
				}).start());

		this.createControlButton("add focus border",
				() -> BrowserDemo.this.browser.addFocusBorder());

		this.createControlButton("remove focus border",
				() -> BrowserDemo.this.browser.removeFocusBorder());

		new Label(composite, SWT.NONE).setText("Exception Handling:");

		this.createControlButton("raise runtime exception", () -> {
			try {
				BrowserDemo.this.browser.run("alert(x);").get();
			} catch (Exception e) {
				log(e);
			}
		});

		this.createControlButton("raise syntax exception", () -> {
			try {
				BrowserDemo.this.browser.run("alert('x);").get();
			} catch (Exception e) {
				log(e);
			}
		});

		this.createControlButton(
				"raise asynchronous runtime exception",
				() -> {
					final JavaScriptExceptionListener javaScriptExceptionListener = javaScriptException -> log(javaScriptException);
					BrowserDemo.this.browser
							.addJavaScriptExceptionListener(javaScriptExceptionListener);
					try {
						BrowserDemo.this.browser
								.run("window.setTimeout(function() { alert(x); }, 50);")
								.get();
						ExecUtils.nonUIAsyncExec(
								(Callable<Void>) () -> {
									BrowserDemo.this.browser
											.removeJavaScriptExceptionListener(javaScriptExceptionListener);
									return null;
								}, 100);
					} catch (Exception e) {
						log("IMPLEMENTATION ERROR - This exception should have be thrown asynchronously!");
						log(e);
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
		this.browser.addMouseMoveListener(e -> log("relative mouse pos " + e.x
				+ "," + e.y));
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
		ExecUtils.nonUIAsyncExec((Callable<Void>) () -> {
			try {
				if (success.get()) {
					log("Page loaded successfully");
				} else {
					log("Page load timed out");
				}
			} catch (Exception e) {
				log(e.getMessage());
			}
			log(ExecUtils.syncExec(() -> BrowserDemo.this.browser.getBrowser()
					.getUrl()));
			return null;
		});
	}
}
