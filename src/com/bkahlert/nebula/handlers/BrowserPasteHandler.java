package com.bkahlert.nebula.handlers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class BrowserPasteHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(BrowserPasteHandler.class);

	private Future<Void> paste(Browser browser) {
		String html = null;
		DataFlavor fileFlavor = null;
		try {
			fileFlavor = new DataFlavor(
					"application/x-java-file-list;class=java.util.List");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			if (clipboard.isDataFlavorAvailable(fileFlavor)) {
				// inserts image files placed in the clipboard
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) clipboard.getData(fileFlavor);
				for (File file : files) {
					try {
						BufferedImage image = ImageIO.read(file);
						html = "<img src=\""
								+ BrowserUtils.createDataUri(image) + "\"/>";
						break;
					} catch (Exception e) {
					}
				}

			} else if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
				// inserts images placed in the clipboard
				org.eclipse.swt.dnd.Clipboard swtClipboard = new org.eclipse.swt.dnd.Clipboard(
						Display.getCurrent());
				ImageData imageData = (ImageData) swtClipboard
						.getContents(ImageTransfer.getInstance());
				html = "<img src=\"" + BrowserUtils.createDataUri(imageData)
						+ "\"/>";
			}
		} catch (Exception e) {
			LOGGER.error("Error pasting clipboard into browser", e);
		}

		if (html != null) {
			return browser.pasteHtmlAtCaret(html);
		}
		return new CompletedFuture<Void>(null, null);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl instanceof org.eclipse.swt.browser.Browser) {
			org.eclipse.swt.browser.Browser swtBrowser = (org.eclipse.swt.browser.Browser) focusControl;
			if (swtBrowser.getParent() instanceof Browser) {
				Browser browser = (Browser) swtBrowser.getParent();
				return this.paste(browser);
			}
		}
		return null;
	}
}
