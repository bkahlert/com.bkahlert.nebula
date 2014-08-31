package com.bkahlert.nebula.widgets.browser;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.browser.BrowserFunction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bkahlert.nebula.widgets.browser.extended.html.Anker;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;

public class BrowserUtils {

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

	public static IAnker extractAnker(String html) {
		if (html == null) {
			return null;
		}
		Document document = Jsoup.parse(html);
		Elements elements = document.getElementsByTag("a");
		for (Element element : elements) {
			if (element.attr("href") == null) {
				element.attr("href", element.attr("data-cke-saved-href"));
			}
			return new Anker(element);
		}
		return null;
	}

	private BrowserUtils() {
	}

	public static String shortenScript(String script) {
		String shortened = script.length() > 100 ? script.substring(0, 100)
				.intern() + "..." : script;
		return shortened.replace("\n", " ").replace("\r", " ")
				.replace("\t", " ");
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
}
