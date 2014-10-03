package com.bkahlert.nebula.gallery.demoSuits.browser;

import java.util.concurrent.Future;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.listener.IDNDListener;

@Demo
public class DragnDropBrowserDemo extends AbstractDemo {

	private Browser browser;

	@Override
	public void createDemo(Composite parent) {
		this.browser = new Browser(parent, SWT.BORDER) {
			@Override
			public void scriptAboutToBeSentToBrowser(String script) {
				log("SENT: " + StringUtils.shorten(script));
			}

			@Override
			public void scriptReturnValueReceived(Object returnValue) {
				log("RETN: " + returnValue);
			}
		};

		final Future<Boolean> loaded = this.browser.openBlank();
		this.browser.injectCss("[draggable] { border: 1px solid #ff0000 }");
		this.browser.injectCss("[droppable] { border: 1px solid #ff00ff }");

		this.browser
				.setBodyHtml("<p>Hello World!</p>"
						+ "<p draggable=\"true\" data-dnd-mime=\"text/html\" data-dnd-data=\"<b>Hello</b> <em>World!</em>\">Drag me!</p>"
						+ "<p droppable=\"true\">Drop here!</p>");
		this.browser.addDNDListener(new IDNDListener() {
			@Override
			public void dragStart(long offsetX, long offsetY, String mimeType,
					String data) {
				log("Dragging started " + offsetX + ", " + offsetY + " ("
						+ mimeType + "): " + data);
			}

			@Override
			public void drop(long offsetX, long offsetY, String mimeType,
					String data) {
				log("Dropped at " + offsetX + ", " + offsetY + " (" + mimeType
						+ "): " + data);
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
	}
}
