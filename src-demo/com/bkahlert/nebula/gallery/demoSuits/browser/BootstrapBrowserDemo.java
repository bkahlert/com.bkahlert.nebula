package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.concurrent.Future;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.browser.listener.IFocusListener;

@Demo
public class BootstrapBrowserDemo extends AbstractDemo {

	private BootstrapBrowser bootstrapBrowser;
	private String html = "<p>Hello <a href=\"#\">World</a>!</p>";

	@Override
	public void createControls(Composite composite) {
		Button setBodyHtml = new Button(composite, SWT.PUSH);
		setBodyHtml.setText("setBodyHtml");
		setBodyHtml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExecUtils.nonUISyncExec(new Runnable() {
					@Override
					public void run() {
						log("setting body html to "
								+ BootstrapBrowserDemo.this.html);
						try {
							BootstrapBrowserDemo.this.bootstrapBrowser
									.setBodyHtml(BootstrapBrowserDemo.this.html)
									.get();
							log("body html set");
						} catch (Exception e) {
							log(e.getMessage());
						}
					}
				});
			}
		});

		Text html = new Text(composite, SWT.BORDER);
		html.setText(this.html + "");
		html.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				BootstrapBrowserDemo.this.html = ((Text) e.getSource())
						.getText();
			}
		});

		Button scrollButton = new Button(composite, SWT.PUSH);
		scrollButton.setText("scroll down");
		scrollButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExecUtils.nonUISyncExec(new Runnable() {
					@Override
					public void run() {
						log("scrolling down " + BootstrapBrowserDemo.this.html);
						try {
							BootstrapBrowserDemo.this.bootstrapBrowser
									.scrollTo(0, 9999).get();
							log("scrolled down");
						} catch (Exception e) {
							log(e.getMessage());
						}
					}
				});
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
		this.bootstrapBrowser = new BootstrapBrowser(parent, SWT.BORDER) {
			@Override
			public void scriptAboutToBeSentToBrowser(String script) {
				log("SENT: " + BrowserUtils.shortenScript(script));
			}

			@Override
			public void scriptReturnValueReceived(Object returnValue) {
				log("RETN: " + returnValue);
			}
		};
		final Future<Boolean> loaded = this.bootstrapBrowser.openAboutBlank();
		this.bootstrapBrowser.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				if (entered) {
					log("Anker hovered over: " + anker);
				} else {
					log("Anker hovered out: " + anker);
				}
			}

			@Override
			public void ankerClicked(IAnker anker) {
				log("Anker clicked: " + anker);
			}
		});
		this.bootstrapBrowser.addFocusListener(new IFocusListener() {
			@Override
			public void focusGained(IElement element) {
				log("Focus gainedr: " + element);
			}

			@Override
			public void focusLost(IElement element) {
				log("Focus lost: " + element);
			}
		});
		ExecUtils.nonUIAsyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					if (loaded.get()) {
						log("loaded successfully");
					} else {
						log("loading failed");
					}
				} catch (Exception e) {
					log("loading error: " + e);
				}
			}
		});
		this.bootstrapBrowser
				.setBodyHtml("<div class=\"container\">"
						+ "<form class=\"form-horizontal\" role=\"form\">"
						+ "<div class=\"form-group\">"
						+ "<label for=\"inputEmail1\" class=\"col-lg-2 control-label\">Email</label>"
						+ "<div class=\"col-lg-10\">"
						+ "<p class=\"form-control-static\"><a href=\"mailto:email@example.com\">email@example.com</a></p>"
						+ "</div>" + "</div>" + "</form>" + "</div>");
	}
}
