package com.bkahlert.devel.nebula.widgets.composer;

import org.eclipse.swt.widgets.Composite;

public class ComposerReadOnly extends Composer {

	public ComposerReadOnly(Composite parent, int style) {
		super(parent, style, 50, ToolbarSet.NONE);
		this.setBackground(parent.getBackground());
		// also stuff done in config.js
		this.setEnabled(false);
	}

	@Override
	public boolean setFocus() {
		return false;
	}

}
