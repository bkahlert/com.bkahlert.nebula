package com.bkahlert.devel.nebula.utils;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ImageUtils {
	public static Dimension getImageDimensions(final File file)
			throws IOException {
		Dimension result = null;

		ImageInputStream imageInputStream = new FileImageInputStream(file);

		Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				reader.setInput(imageInputStream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				result = new Dimension(width, height);
			} catch (IOException e) {
				throw e;
			} finally {
				reader.dispose();
			}
		} else {
			System.err.println("No reader found for given file: " + file);
		}
		return result;
	}

	/**
	 * Returns a new scaled image. new Image must be disposed after use.
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image resize(Image image, int width, int height) {
		if (image == null) {
			return null;
		}

		final Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width,
				image.getBounds().height, 0, 0, width, height);
		gc.dispose();

		return scaled;
	}

	/**
	 * Returns a new scaled image. The new Image must be disposed after use.
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image resize(ImageData imageData, int width, int height) {
		if (imageData == null) {
			return null;
		}

		if (imageData.width == width && imageData.height == height) {
			return new Image(Display.getDefault(), imageData);
		}

		Image tmpImage = null;
		Image fullImage = new Image(Display.getCurrent(), imageData);
		tmpImage = resize(fullImage, width, height);

		fullImage.dispose();
		return tmpImage;
	}

	public static ImageData resize(ImageData imageData, int width, int height,
			boolean antiAliasing) {
		if (antiAliasing) {
			Image tmpImage = resize(imageData, width, height);
			ImageData result = tmpImage.getImageData();
			tmpImage.dispose();
			return result;
		}
		return imageData.scaledTo(width, height);
	}

	/**
	 * Converts a SWT {@link Image} to an AWT {@link BufferedImage}.
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage convertToAWT(Image image) {
		return convertToAWT(image.getImageData());
	}

	/**
	 * Converts a SWT {@link ImageData} to an AWT {@link BufferedImage}.
	 * 
	 * @param data
	 * @return
	 */
	public static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
					palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					raster.setPixels(x, y, 1, 1, pixelArray);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * Returns a Base64-encoded {@link String} that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param data
	 * @return
	 */
	public static String convertToInlineSrc(Image image) {
		return convertToInlineSrc(image.getImageData());
	}

	/**
	 * Returns a Base64-encoded {@link String} that can be used for the
	 * <code>src</code> attribute of an HTML <code>img</code>.
	 * 
	 * @param data
	 * @return
	 */
	public static String convertToInlineSrc(ImageData data) {
		BufferedImage x = ImageUtils.convertToAWT(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(x, "png", baos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] encodedImage = Base64.encode(baos.toByteArray());
		try {
			return "data:image/png;base64," + new String(encodedImage, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Point resizeWithinArea(Point size, Point maxSize) {

		int finalWidth = maxSize.x;
		int finalHeight = maxSize.y;

		if (size.y > maxSize.y || size.x > maxSize.x) {
			float ratio = (float) size.x / (float) size.y;
			finalHeight = maxSize.y;
			finalWidth = Math.round(finalHeight * ratio);

			if (finalWidth > maxSize.x) {
				finalHeight = Math.round(maxSize.x / ratio);
				finalWidth = maxSize.x;
			}
		}

		return new Point(finalWidth, finalHeight);
	}
}
