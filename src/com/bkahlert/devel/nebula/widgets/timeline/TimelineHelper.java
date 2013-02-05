package com.bkahlert.devel.nebula.widgets.timeline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class TimelineHelper {

	private static final Logger LOGGER = Logger.getLogger(TimelineHelper.class);
	private static Map<Image, URI> imageUris = new HashMap<Image, URI>();

	public static URI createUriFromImage(Image image) {
		if (image == null)
			return null;
		if (!imageUris.containsKey(image)) {
			try {
				File file = File.createTempFile(
						TimelineHelper.class.getSimpleName(), ".png");
				file.deleteOnExit();
				FileOutputStream outputStream = new FileOutputStream(file);
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { image.getImageData() };
				loader.save(outputStream, SWT.IMAGE_PNG);
				outputStream.close();
				imageUris.put(image,
						new URI("file://" + file.getAbsolutePath()));
			} catch (IOException e) {
				LOGGER.error("Error making copy of image", e);
			} catch (URISyntaxException e) {
				LOGGER.error("Error generating " + URI.class.getSimpleName()
						+ " to image", e);
			}
		}
		return imageUris.get(image);
	}
}
