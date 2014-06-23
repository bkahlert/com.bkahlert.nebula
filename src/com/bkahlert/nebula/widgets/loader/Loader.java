package com.bkahlert.nebula.widgets.loader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class Loader {

	private Shell shell = null;
	private final Control composite;

	private final ControlListener controlListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e) {
			Loader.this.updateBounds();
		}

		@Override
		public void controlMoved(ControlEvent e) {
			Loader.this.updateBounds();
		}
	};

	public Loader(Control widget) {
		super();
		this.composite = widget;
	}

	public void start() {
		if (this.shell == null) {
			this.shell = new Shell(SWT.NO_TRIM | SWT.ON_TOP);
			this.shell.setLayout(new FillLayout());
			new LoaderComposite(this.shell);
		}
		this.activate();
		this.updateBounds();
		this.shell.setAlpha(125);
		this.shell.open();
	}

	protected void updateBounds() {
		this.shell.setSize(this.composite.getSize());
		this.shell.setLocation(this.composite.toDisplay(0, 0));
	}

	protected void activate() {
		Control control = this.composite;
		while (control != null) {
			control.addControlListener(this.controlListener);
			control = control.getParent();
		}
	}

	protected void deactivate() {
		Control control = this.composite;
		while (control != null) {
			control.removeControlListener(this.controlListener);
			control = control.getParent();
		}
	}

	public void stop() {
		this.deactivate();
		if (this.shell != null && !this.shell.isDisposed()) {
			this.shell.setVisible(false);
		}
	}

	public void dispose() {
		this.deactivate();
		if (this.shell != null && !this.shell.isDisposed()) {
			this.shell.close();
			this.shell.dispose();
		}
	}

}
