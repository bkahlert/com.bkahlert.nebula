package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.FontUtils;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.ColorPicker;
import com.bkahlert.nebula.widgets.ColorPicker.IModificationListener;
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

		this.bootstrapBrowser = new BootstrapBrowser(sash, SWT.INHERIT_FORCE);
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
						.scaleSaturationBy(rgb, saturationFactor);
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

		Composite mixer = new Composite(sash, SWT.NONE);
		mixer.setLayout(new FillLayout(SWT.VERTICAL));

		this.colorPicker1 = new ColorPicker(mixer);
		this.colorPicker1.addModificationListener(new IModificationListener() {
			@Override
			public void colorChanged(RGB rgb) {
				ColorUtilsDemo.this.updateColorPicker();
			}
		});

		Label plus = new Label(mixer, SWT.CENTER);
		plus.setText("+");
		FontUtils.changeFontSizeBy(plus, 100);

		this.colorPicker2 = new ColorPicker(mixer);
		this.colorPicker2.addModificationListener(new IModificationListener() {
			@Override
			public void colorChanged(RGB rgb) {
				ColorUtilsDemo.this.updateColorPicker();
			}
		});

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

	private void updateColorPicker() {
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .25));
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .5));
		this.resultColorPicker1.setRGB(this.colorPicker1.getRGB().mix(
				this.colorPicker2.getRGB(), .75));
	}
}
