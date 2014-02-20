package com.bkahlert.nebula.widgets.chatControl.items;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.SimpleRoundedComposite;

/**
 * This composite is used to display a separator between messages of the same
 * chat partner.
 * 
 * @author bkahlert
 */
public class ChatLineSeparator extends SimpleRoundedComposite {

	protected final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"dd/MM/yy HH:mm");

	public ChatLineSeparator(Composite parent, String username, Color color,
			Date date) {
		super(parent, SWT.SEPARATOR);
		this.setBackground(color);

		String receivedOn = this.dateFormatter.format(date);
		this.setTexts(new String[] { receivedOn });
	}

}
