package com.bkahlert.devel.nebula.widgets.timeline.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import com.bkahlert.devel.nebula.widgets.timeline.ITimelineEvent;

/**
 * This implementation of {@link ITimelineEvent} accepts {@link Image} instead
 * of strings for the icon and image fields.
 * <p>
 * Internally it saves the {@link Image} to a file and uses the file to generate
 * an {@link URI} to it. Multiple instances of the same {@link Image} only use a
 * single file.
 * 
 * @author bkahlert
 * 
 */
public class TimelineEventImageBased extends TimelineEvent {

	private static final Logger LOGGER = Logger
			.getLogger(TimelineEventImageBased.class);
	private static Map<Image, URI> imageUris = new HashMap<Image, URI>();

	private static URI createUri(Image image) throws IOException,
			URISyntaxException {
		if (image == null)
			return null;
		if (!imageUris.containsKey(image)) {
			File file = File.createTempFile(
					TimelineEventImageBased.class.getSimpleName(), ".png");
			file.deleteOnExit();
			FileOutputStream outputStream = new FileOutputStream(file);
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { image.getImageData() };
			loader.save(outputStream, SWT.IMAGE_PNG);
			outputStream.close();
			imageUris.put(image, new URI("file://" + file.getAbsolutePath()));
		}
		return imageUris.get(image);
	}

	private URI icon = null;
	private URI image = null;

	public TimelineEventImageBased(String title, Image icon, Image image,
			Calendar start, Calendar end, List<String> classNames,
			Object payload) {
		super(title, null, null, start, end, classNames, payload);

		try {
			this.icon = createUri(icon);
		} catch (IOException e) {
			LOGGER.error("Error making copy of icon", e);
		} catch (URISyntaxException e) {
			LOGGER.error("Error generating " + URI.class.getSimpleName()
					+ " to icon", e);
		}
		try {
			this.image = createUri(image);
		} catch (IOException e) {
			LOGGER.error("Error making copy of image", e);
		} catch (URISyntaxException e) {
			LOGGER.error("Error generating " + URI.class.getSimpleName()
					+ " to image", e);
		}
	}

	@Override
	public String getIcon() {
		return this.icon != null ? this.icon.toString() : null;
	}

	@Override
	public String getImage() {
		return this.image != null ? this.image.toString() : null;
	}
}
