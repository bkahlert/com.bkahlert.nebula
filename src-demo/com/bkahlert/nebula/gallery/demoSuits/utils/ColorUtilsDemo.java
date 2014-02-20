package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.ColorPicker;

@Demo
public class ColorUtilsDemo extends AbstractDemo {

	@Override
	public void createDemo(Composite parent) {
		List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0, 255),
				new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
				new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255,
						255), new RGB(127, 127, 127), new RGB(127, 127, 255),
				new RGB(127, 255, 127), new RGB(127, 255, 255), new RGB(255,
						127, 127), new RGB(255, 127, 255), new RGB(255, 255,
						127), new RGB(232, 232, 232));
		Composite colorComposite = new Composite(parent, SWT.BORDER);
		colorComposite.setLayout(new GridLayout(colors.size(), true));
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.2f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.1f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			new ColorPicker(colorComposite, rgb);
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					1.0f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.9f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.8f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.7f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.2f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.1f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(rgbx,
					0.0f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					1.0f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.9f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.8f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.7f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.6f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.5f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.4f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.3f).toClassicRGB());
		}
		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.2f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.1f).toClassicRGB());
		}

		for (RGB rgb : colors) {
			com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
					rgb);
			new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(rgbx,
					0.0f).toClassicRGB());
		}
	}
}
