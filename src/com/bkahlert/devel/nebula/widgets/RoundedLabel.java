package com.bkahlert.devel.nebula.widgets;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class RoundedLabel extends RoundedComposite {

	private Label label;

	public RoundedLabel(Composite parent, int style) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.swtDefaults().margins(2, 2).create());
		this.label = new Label(this, SWT.NONE);
	}

	public void setText(String text) {
		this.label.setText(text);
	}

	public String getText() {
		return this.label.getText();
	}

}
