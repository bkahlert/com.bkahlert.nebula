package com.bkahlert.devel.nebula.images;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.PaintUtils;

public class Images {

	/**
	 * Returns a new {@link Image} where all pixels with the color of the pixel
	 * at the specified positon are transparent.
	 * <p>
	 * <strong>Attention: This creates a new {@link Image} that needs to be
	 * disposed independently of the given one.</strong>
	 * 
	 * @param image
	 *            that serves as the basis for the generated copy
	 * @param x
	 *            coordinate of the pixel to be taken the color from
	 * @param y
	 *            coordinate of the pixel to be taken the color from
	 * @return
	 */
	public static Image createTransparentImage(Image image, int x, int y) {
		ImageData transparentImageData = image.getImageData();
		transparentImageData.transparentPixel = transparentImageData.palette
				.getPixel(transparentImageData.palette
						.getRGB(transparentImageData.getPixel(0, 0)));
		return new Image(Display.getCurrent(), transparentImageData);
	}

	public static Image getDot(int width, int height, RGB background, RGB border) {
		Image image = new Image(Display.getCurrent(), width, height);
		GC gc = new GC(image);
		gc.setAdvanced(true);

		Color backgroundColor = new Color(Display.getCurrent(),
				background.toClassicRGB());
		Color borderColor = border != null ? new Color(Display.getCurrent(),
				border.toClassicRGB()) : null;
		if (border != null) {
			PaintUtils.drawRoundedRectangle(gc, new Rectangle(0, 0, width,
					height), backgroundColor, borderColor);
		} else {
			PaintUtils.drawRoundedRectangle(gc, new Rectangle(0, 0, width,
					height), backgroundColor);
		}

		if (borderColor != null)
			borderColor.dispose();
		backgroundColor.dispose();
		gc.dispose();

		Image transparentImage = createTransparentImage(image, 0, 0);
		image.dispose();

		return transparentImage;
	}

	public static Image getDot(int width, int height, RGB background,
			Float borderLightness) {
		RGB border = borderLightness != null ? ColorUtils.addLightness(
				background, borderLightness) : null;
		return getDot(width, height, background, border);
	}

	public static ImageDescriptor getOverlayDot(RGB color) {
		Assert.isNotNull(color);
		Image image = getDot(6, 6, color, ColorUtils.addLightness(color, -0.1f));
		ImageDescriptor imageDescriptor = ImageDescriptor
				.createFromImageData(image.getImageData());
		image.dispose();
		return imageDescriptor;
	}

}