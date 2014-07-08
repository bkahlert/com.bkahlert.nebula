package com.bkahlert.nebula.gallery.demoSuits.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.ColorPicker;

@Demo
public class ColorUtilsDemo extends AbstractDemo {

	@Override
	public void createControls(Composite composite) {
		Button newRandomColor = new Button(composite, SWT.PUSH);
		newRandomColor.setText("Random");
		newRandomColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
	}

	@Override
	public void createDemo(Composite parent) {
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
		Composite colorComposite = new Composite(parent, SWT.BORDER);
		colorComposite.setLayout(new GridLayout(colors.size(), true));

		for (float lightnessFactor = 1.6f; lightnessFactor >= 0f; lightnessFactor -= 0.1f) {
			for (RGB rgb : colors) {
				com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
						rgb);
				new ColorPicker(colorComposite, ColorUtils.scaleLightnessBy(
						rgbx, lightnessFactor).toClassicRGB());
			}
		}

		for (float saturationFactor = 1.0f; saturationFactor >= 0f; saturationFactor -= 0.1f) {
			for (RGB rgb : colors) {
				com.bkahlert.nebula.utils.colors.RGB rgbx = new com.bkahlert.nebula.utils.colors.RGB(
						rgb);
				new ColorPicker(colorComposite, ColorUtils.scaleSaturationBy(
						rgbx, saturationFactor).toClassicRGB());
			}
		}
	}
}
