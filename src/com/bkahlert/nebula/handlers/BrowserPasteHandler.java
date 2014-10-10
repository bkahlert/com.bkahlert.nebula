package com.bkahlert.nebula.handlers;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.CompletedFuture;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class BrowserPasteHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger
			.getLogger(BrowserPasteHandler.class);

	// @see
	// http://blog.pengoworks.com/index.cfm/2008/2/8/The-nightmares-of-getting-images-from-the-Mac-OS-X-clipboard-using-Java
	private static BufferedImage getBufferedImage(Image img) {
		if (img == null) {
			return null;
		}
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		// draw original image to thumbnail image object and
		// scale it to the new size on-the-fly
		BufferedImage bufimg = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bufimg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(img, 0, 0, w, h, null);
		g2.dispose();
		return bufimg;
	}

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
				ImageIcon image = new ImageIcon(
						(BufferedImage) clipboard
								.getData(DataFlavor.imageFlavor));
				BufferedImage bImage = getBufferedImage(image.getImage());
				html = "<img src=\"" + BrowserUtils.createDataUri(bImage)
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
