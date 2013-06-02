package com.bkahlert.nebula.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {

	private HttpUtils() {
	}

	public static int checkResponseCode(final String url)
			throws MalformedURLException, IOException {
		return getResponseCode(new URL(url));
	}

	public static int getResponseCode(final URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		return connection.getResponseCode();
	}

}
