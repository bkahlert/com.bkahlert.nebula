package com.bkahlert.nebula.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ToolTip extends Shell {
	private Label label;

	public ToolTip() {
		super(SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
		this.setBackground(getDisplay().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));

		FillLayout layout = new FillLayout();
		layout.marginWidth = 2;
		this.setLayout(layout);

		this.label = new Label(this, SWT.NONE);
		this.label.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_INFO_FOREGROUND));
		this.label.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_INFO_BACKGROUND));
	}

	public void show(String text, Point location) {
		this.label.setText(text);
		this.pack();
		this.setLocation(location);
		this.setVisible(true);
	}

	public void hide() {
		this.setVisible(false);
	}
}
