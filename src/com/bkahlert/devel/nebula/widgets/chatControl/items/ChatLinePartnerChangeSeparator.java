package com.bkahlert.devel.nebula.widgets.chatControl.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.SimpleRoundedComposite;

/**
 * This composite is used to display a separator between messages of different
 * chat partners.
 * 
 * @author bkahlert
 */
public class ChatLinePartnerChangeSeparator extends SimpleRoundedComposite {

	protected final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd/MM/yy HH:mm");

	public ChatLinePartnerChangeSeparator(Composite parent, String username,
			Color color, Date date) {
		super(parent, SWT.NONE);
		this.setBackground(color);

		String receivedOn = this.dateFormatter.format(date);
		this.setTexts(new String[] { username, receivedOn });
	}

	public String getPlainID() {
		return this.usedText[0];
	}
}
