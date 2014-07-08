package com.bkahlert.nebula.widgets.chatControl.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * This composite is used to display a chat message.
 * 
 * @author bkahlert
 */
public class ChatLine extends Composite {
	protected StyledText text;

	public ChatLine(Composite parent, String message) {
		super(parent, SWT.NONE);
		this.setLayout(new FillLayout());

		this.text = new StyledText(this, SWT.WRAP);
		this.text.setText(message);
		this.text.setEditable(false);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		this.text.setBackground(color);
	}

	public String getText() {
		return this.text.getText();
	}
}
