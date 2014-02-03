package com.bkahlert.devel.nebula.widgets.composer;

import org.eclipse.swt.widgets.Composite;

public class ComposerReadOnly extends Composer {

	private Composite composite = null;

	public ComposerReadOnly(Composite parent, int style) {
		super(parent, style, 50, ToolbarSet.NONE);
		this.composite = parent;
		this.setBackground(composite.getBackground());
		this.setEnabled(false);
	}

	@Override
	public boolean setFocus() {
		return false;
	}

}
