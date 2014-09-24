package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;

@Demo
public class ColorUtilsDemo extends AbstractDemo {

	private BootstrapBrowser bootstrapBrowser;

	@Override
	public void createDemo(Composite parent) {
		this.bootstrapBrowser = new BootstrapBrowser(parent, SWT.INHERIT_FORCE);
		this.bootstrapBrowser.openBlank();

		List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0, 255),
				new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
				new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255,
						255), new RGB(127, 127, 127), new RGB(127, 127, 255),
				new RGB(127, 255, 127), new RGB(127, 255, 255), new RGB(255,
						127, 127), new RGB(255, 127, 255), new RGB(255, 255,
						127), new RGB(232, 232, 232), ColorUtils.getRandomRGB()
						.toClassicRGB(), ColorUtils.getRandomRGB()
						.toClassicRGB(), ColorUtils.getRandomRGB()
						.toClassicRGB());

		StringBuilder html = new StringBuilder();
		html.append("<table style=\"width: 100%;\">");

		int numRows = 0;
		for (float lightnessFactor = 1.6f; lightnessFactor >= 0f; lightnessFactor -= 0.1f) {
			numRows++;
			html.append("<tr>");
			for (RGB rgb : colors) {
				com.bkahlert.nebula.utils.colors.RGB color = ColorUtils
						.scaleLightnessBy(
								new com.bkahlert.nebula.utils.colors.RGB(rgb),
								lightnessFactor);
				html.append("<td style=\"background-color: "
						+ color.toHexString()
						+ "; min-width: %{minwidth}; height: %{height};\"></td>");
			}
			html.append("</tr>");
		}
		for (float saturationFactor = 1.0f; saturationFactor >= 0f; saturationFactor -= 0.1f) {
			numRows++;
			html.append("<tr>");
			for (RGB rgb : colors) {
				com.bkahlert.nebula.utils.colors.RGB color = ColorUtils
						.scaleSaturationBy(
								new com.bkahlert.nebula.utils.colors.RGB(rgb),
								saturationFactor);
				html.append("<td style=\"background-color: "
						+ color.toHexString()
						+ "; min-width: %{minwidth}; height: %{height};\"></td>");
			}
			html.append("</tr>");
		}

		html.append("</table>");
		this.bootstrapBrowser.setBodyHtml(html.toString()
				.replace("%{minwidth}", "1em")
				.replace("%{height}", (100.0 / numRows) + "vh"));
	}
}
