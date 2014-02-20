package com.bkahlert.nebula.widgets.composer;

import org.eclipse.swt.widgets.Composite;

public class ComposerReadOnly extends Composer {

	private Composite composite = null;

	public ComposerReadOnly(Composite parent, int style) {
		super(parent, style, 50, ToolbarSet.NONE);
		this.composite = parent;
		this.setEnabled(false);
		this.setBackground(this.composite.getBackground());
	}

	@Override
	public boolean setFocus() {
		return false;
	}

	@Override
	public void scriptAboutToBeSentToBrowser(String script) {
		System.err.println("SENT " + script);
	}

	@Override
	public void scriptReturnValueReceived(Object returnValue) {
		System.err.println("RET " + returnValue);
	}

}
