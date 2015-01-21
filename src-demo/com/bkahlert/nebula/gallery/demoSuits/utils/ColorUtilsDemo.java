package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.FontUtils;
import com.bkahlert.nebula.utils.Pair;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.ColorPicker;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;

@Demo
public class ColorUtilsDemo extends AbstractDemo {

	private BootstrapBrowser bootstrapBrowser;
	private ColorPicker colorPicker1;
	private ColorPicker colorPicker2;
	private ColorPicker resultColorPicker1;
	private ColorPicker resultColorPicker2;
	private ColorPicker resultColorPicker3;

	@Override
	public void createDemo(Composite parent) {
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);

		this.allColors(sash);
		this.mixerDemo(sash);
		this.rainbow(sash);
	}

	private void allColors(Composite composite) {
		this.bootstrapBrowser = new BootstrapBrowser(composite,
				SWT.INHERIT_FORCE);
		this.bootstrapBrowser.openBlank();

		List<RGB> colors = Arrays.asList(new RGB(0, 0, 0), new RGB(0, 0, 255),
				new RGB(0, 255, 0), new RGB(0, 255, 255), new RGB(255, 0, 0),
				new RGB(255, 0, 255), new RGB(255, 255, 0), new RGB(255, 255,
						255), new RGB(127, 127, 127), new RGB(127, 127, 255),
				new RGB(127, 255, 127), new RGB(127, 255, 255), new RGB(255,
						127, 127), new RGB(255, 127, 255), new RGB(255, 255,
						127), new RGB(232, 232, 232),
				ColorUtils.getRandomRGB(), ColorUtils.getRandomRGB(),
				ColorUtils.getRandomRGB());

		Pair<Integer, String> table = this.renderTable(colors, 1, true);

		this.bootstrapBrowser.setBodyHtml(table.getSecond());
	}

	private void mixerDemo(Composite composite) {
		Composite mixer = new Composite(composite, SWT.NONE);
		mixer.setLayout(new FillLayout(SWT.VERTICAL));

		this.colorPicker1 = new ColorPicker(mixer);
		this.colorPicker1.addModificationListener(rgb -> ColorUtilsDemo.this
				.updateColorPicker());

		Label plus = new Label(mixer, SWT.CENTER);
		plus.setText("+");
		FontUtils.changeFontSizeBy(plus, 100);

		this.colorPicker2 = new ColorPicker(mixer);
		this.colorPicker2.addModificationListener(rgb -> ColorUtilsDemo.this
				.updateColorPicker());

		Label equals = new Label(mixer, SWT.CENTER);
		equals.setText("=");
		FontUtils.changeFontSizeBy(equals, 100);

		Composite result = new Composite(mixer, SWT.NONE);
		result.setLayout(new FillLayout(SWT.HORIZONTAL));

		this.resultColorPicker1 = new ColorPicker(result);
		this.resultColorPicker1.setEnabled(false);
		this.resultColorPicker2 = new ColorPicker(result);
		this.resultColorPicker2.setEnabled(false);
		this.resultColorPicker3 = new ColorPicker(result);
		this.resultColorPicker3.setEnabled(false);
		this.updateColorPicker();
	}

	private void rainbow(Composite composite) {
		BootstrapBrowser bootstrapBrowser = new BootstrapBrowser(composite,
				SWT.INHERIT_FORCE);
		bootstrapBrowser.openBlank();

		int numColors = 2;
		int numRuns = 3;

		List<HLS> colors = ColorUtils.rainbow(numColors);

		String html = this.renderTable(
				colors.stream().map(hls -> ColorSpaceConverter.HLStoRGB(hls))
						.collect(Collectors.toList()), numRuns + 1, false)
				.getSecond();
		for (int run = 0; run < numRuns; run++) {
			numColors = colors.size();
			List<HLS> newColors = new LinkedList<>();
			for (int i = 0; i < numColors; i++) {
				newColors.addAll(ColorUtils.rainbow(numColors, colors, i));
			}
			colors = newColors;
			html += this.renderTable(
					colors.stream()
							.map(hls -> ColorSpaceConverter.HLStoRGB(hls))
							.collect(Collectors.toList()), numRuns + 1, false)
					.getSecond();
		}

		bootstrapBrowser.setBodyHtml(html);
	}

	private Pair<Integer, String> renderTable(Collection<RGB> colors,
			int pages, boolean showSaturation) {
		StringBuilder html = new StringBuilder();
		html.append("<table style=\"width: 100%;\">");

		int numRows = 0;
		for (float lightnessFactor = 1.6f; lightnessFactor >= 0f; lightnessFactor -= 0.1f) {
			numRows++;
			html.append("<tr>");
			for (RGB rgb : colors) {
				com.bkahlert.nebula.utils.colors.RGB color = ColorUtils
						.scaleLightnessBy(rgb, lightnessFactor);
				html.append("<td style=\"background-color: "
						+ color.toDecString()
						+ "; min-width: %{minwidth}; height: %{height};\"></td>");
			}
			html.append("</tr>");
		}
		if (showSaturation) {
			for (float saturationFactor = 1.0f; saturationFactor >= 0f; saturationFactor -= 0.1f) {
				numRows++;
				html.append("<tr>");
				for (RGB rgb : colors) {
					com.bkahlert.nebula.utils.colors.RGB color = ColorUtils
							.scaleSaturationBy(rgb, saturationFactor);
					html.append("<td style=\"background-color: "
							+ color.toDecString()
							+ "; min-width: %{minwidth}; height: %{height};\"></td>");
				}
				html.append("</tr>");
			}
		}

		html.append("</table>");
		return new Pair<>(numRows, html.toString().replace("%{minwidth}", "0")
				.replace("%{height}", ((100.0 / pages) / numRows) + "vh"));
	}

	private void updateColorPicker() {
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .25));
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .5));
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .75));
	}
}
