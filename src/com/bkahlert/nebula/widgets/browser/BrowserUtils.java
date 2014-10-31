package com.bkahlert.nebula.widgets.browser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

@SuppressWarnings("restriction")
public class BrowserUtils {

	private static final String TRACK_ATTR_NAME = "data-nebula-track";
	private static final String TRACK_ATTR_VALUE = "true";

	private static Pattern TAG_NAME_PATTERN = Pattern.compile(
			"^[^<]*<(\\w+)[^<>]*\\/?>.*", Pattern.DOTALL);

	/**
	 * Returns the first tag name that could be found in the given HTML code.
	 * 
	 * @param html
	 * @return
	 */
	public static String getFirstTagName(String html) {
		if (html == null) {
			return null;
		}
		Matcher matcher = TAG_NAME_PATTERN.matcher(html);
		if (!matcher.matches()) {
			return null;
		}
		if (matcher.groupCount() != 1) {
			return null;
		}
		return matcher.group(1);
	}

	public static File getFile(Class<?> clazz, String clazzRelativePath) {
		String uri = getFileUrl(clazz, clazzRelativePath).toString();
		if (uri.startsWith("file://")) {
			return new File(uri.substring("file://".length()));
		}
		return null;
	}

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath) {
		return getFileUrl(clazz, clazzRelativePath, "");
	}

	/**
	 * Returns the container url for this class. This varies based on whether or
	 * not the class files are in a zip/jar or not, so this method standardizes
	 * that. The method may return null, if the class is a dynamically generated
	 * class (perhaps with asm, or a proxy class)
	 * 
	 * @param c
	 *            The class to find the container for
	 * @return
	 */
	public static String GetClassContainer(Class<?> c) {
		if (c == null) {
			throw new NullPointerException(
					"The Class passed to this method may not be null");
		}
		try {
			while (c.isMemberClass() || c.isAnonymousClass()) {
				c = c.getEnclosingClass(); // Get the actual enclosing file
			}
			if (c.getProtectionDomain().getCodeSource() == null) {
				// This is a proxy or other dynamically generated class, and has
				// no physical container,
				// so just return null.
				return null;
			}
			String packageRoot;
			try {
				// This is the full path to THIS file, but we need to get the
				// package root.
				String thisClass = c.getResource(c.getSimpleName() + ".class")
						.toString();
				packageRoot = StringUtils.replace(
						thisClass,
						Pattern.quote(c.getName().replaceAll("\\.", "/")
								+ ".class"), "");
				if (packageRoot.endsWith("!/")) {
					packageRoot = StringUtils.replace(packageRoot, "!/", "");
				}
			} catch (Exception e) {
				// Hmm, ok, try this then
				packageRoot = c.getProtectionDomain().getCodeSource()
						.getLocation().toString();
			}
			packageRoot = URLDecoder.decode(packageRoot, "UTF-8");
			return packageRoot;
		} catch (Exception e) {
			throw new RuntimeException("While interrogating " + c.getName()
					+ ", an unexpected exception was thrown.", e);
		}
	}

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath,
			String suffix) {
		try {
			URI uri = new URI((new File(GetClassContainer(clazz)).getParent()
					+ "/" + clazzRelativePath + suffix).replace("file:",
					"file://"));
			if (!uri.toString().contains("bundleresource:")) {
				return uri;
			}
			URL url = FileLocator.toFileURL(clazz
					.getResource(clazzRelativePath));
			String timelineUrlString = url.toString().replace("file:",
					"file://");
			return new URI(timelineUrlString + suffix);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean fuzzyEquals(String uri1, String uri2) {
		if (uri1 == null && uri2 == null) {
			return true;
		}
		if (uri1 == null && uri2 != null) {
			return false;
		}
		if (uri1 != null && uri2 == null) {
			return false;
		}
		if (uri1.endsWith(uri2) || uri2.endsWith(uri1)) {
			return true;
		}
		return false;
	}

	public static IElement extractElement(String html) {
		if (html == null) {
			return null;
		}
		String tagName = getFirstTagName(html);
		if (tagName == null) {
			return null;
		}

		// add attribute to make the element easily locatable
		String trackAttr = " " + TRACK_ATTR_NAME + "=\"" + TRACK_ATTR_VALUE
				+ "\"";
		if (html.endsWith("/>")) {
			html = html.substring(0, html.length() - 2) + trackAttr + "/>";
		} else {
			html = html.replaceFirst(">", trackAttr + ">");
		}

		// add missing tags, otherwise JSoup will simply delete those
		// "mis-placed" tags
		if (tagName.equals("td")) {
			html = "<table><tbody><tr>" + html + "</tr></tbody></table>";
		} else if (tagName.equals("tr")) {
			html = "<table><tbody>" + html + "</tbody></table>";
		} else if (tagName.equals("tbody")) {
			html = "<table>" + html + "</table>";
		}

		Document document = Jsoup.parse(html);
		Element element = document.getElementsByAttributeValue(TRACK_ATTR_NAME,
				TRACK_ATTR_VALUE).first();
		element.removeAttr(TRACK_ATTR_NAME);
		if (element.attr("href") == null) {
			element.attr("href", element.attr("data-cke-saved-href"));
		}
		return new com.bkahlert.nebula.widgets.browser.extended.html.Element(
				element);
	}

	private BrowserUtils() {
	}

	/**
	 * Creates a random name for a JavaScript function. This is especially handy
	 * for callback functions injected by {@link BrowserFunction}.
	 * 
	 * @return
	 */
	public static String createRandomFunctionName() {
		return "_" + new BigInteger(130, new SecureRandom()).toString(32);
	}

	/**
	 * Returns a Base64-encoded {@link String} data URI that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param file
	 *            must point to a readable image file
	 * @return
	 */
	public static String createDataUri(File file) throws IOException {
		return createDataUri(ImageIO.read(file));
	}

	/**
	 * Returns a Base64-encoded {@link String} data URI that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param image
	 * @return
	 */
	public static String createDataUri(Image image) {
		return createDataUri(image.getImageData());
	}

	/**
	 * Returns a Base64-encoded {@link String} data URI that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param data
	 * @return
	 */
	public static String createDataUri(ImageData data) {
		return createDataUri(ImageUtils.convertToAWT(data));
	}

	/**
	 * Returns a Base64-encoded {@link String} data URI that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param image
	 * @return
	 */
	public static String createDataUri(BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] encodedImage = Base64.encode(baos.toByteArray());
		try {
			return "data:image/png;base64," + new String(encodedImage, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
