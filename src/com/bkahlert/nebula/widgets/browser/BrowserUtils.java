package com.bkahlert.nebula.widgets.browser;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
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

	public static URI getFileUrl(Class<?> clazz, String clazzRelativePath,
			String suffix) {
		try {
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
				+ "..." : script;
		return shortened.replace("\n", " ").replace("\r", " ")
				.replace("\t", " ");
	}
}
