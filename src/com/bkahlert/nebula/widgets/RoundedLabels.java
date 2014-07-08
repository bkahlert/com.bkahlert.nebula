package com.bkahlert.nebula.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.colors.ColorUtils;

public class RoundedLabels extends RoundedComposite {

	private boolean border;

	private Color bgColor;
	private Color labelBgColor;

	// TODO SWT.BORDER an labels weiterreichen
	public RoundedLabels(Composite parent, int style, RGB backgroundColor) {
		super(parent, style);

		this.border = (style & SWT.BORDER) != 0;

		this.bgColor = this.borderColor = new Color(Display.getDefault(),
				ColorUtils.scaleLightnessBy(
						new com.bkahlert.nebula.utils.colors.RGB(
								backgroundColor), 1.2f).toClassicRGB());
		this.labelBgColor = new Color(Display.getCurrent(), backgroundColor);

		this.setBackground(parent.getBackground());

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!bgColor.isDisposed())
					bgColor.dispose();
				if (!labelBgColor.isDisposed())
					labelBgColor.dispose();
			}
		});
	}

	private void clear() {
		for (Control control : this.getChildren()) {
			if (!control.isDisposed())
				control.dispose();
		}
		layout();
	}

	public void setTexts(String[] strings) {
		clear();
		int style = SWT.WRAP;
		if (this.border)
			style |= SWT.BORDER;
		for (String string : strings) {
			RoundedLabel roundedLabel = new RoundedLabel(this, style);
			roundedLabel.setBackgroundMode(SWT.INHERIT_DEFAULT);
			roundedLabel.setBackground(labelBgColor);
			roundedLabel.setText(string);
		}
		this.setLayout(new RowLayout());
		layout();
	}

	public String[] getTexts() {
		Control[] roundedLabels = this.getChildren();
		String[] strings = new String[roundedLabels.length];
		for (int i = 0, j = roundedLabels.length; i < j; i++) {
			strings[i] = ((RoundedLabel) roundedLabels[i]).getText();
		}
		return strings;
	}

}
