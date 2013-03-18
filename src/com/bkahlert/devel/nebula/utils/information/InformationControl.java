package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This is a typed version of the {@link IInformationControlExtension2}.<br>
 * Instead of having to override
 * {@link IInformationControlExtension2#setInput(Object)} you have to override
 * {@link #load(Object)} that returns a boolean whether to show information or
 * not.
 * <p>
 * <strong>Make sure to override
 * {@link #getInformationPresenterControlCreator()} if you want to allow the
 * user to hover the popup and get an enhanced version.</strong>
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public abstract class InformationControl<INFORMATION> extends
		AbstractInformationControl implements IInformationControlExtension2 {

	private boolean hasContents = false;

	protected InformationControl(Shell parentShell, String statusFieldText,
			Object noCreate) {
		super(parentShell, statusFieldText);
	}

	protected InformationControl(Shell parentShell,
			ToolBarManager toolBarManager, Object noCreate) {
		super(parentShell, toolBarManager);
	}

	public InformationControl(Shell parentShell, String statusFieldText) {
		super(parentShell, statusFieldText);
		this.create();
	}

	public InformationControl(Shell parentShell, ToolBarManager toolBarManager) {
		super(parentShell, toolBarManager);
		this.create();
	}

	@Override
	protected abstract void createContent(Composite parent);

	@SuppressWarnings("unchecked")
	@Override
	public final void setInput(Object input) {
		try {
			this.hasContents = this.load((INFORMATION) input);
		} catch (ClassCastException e) {
			this.hasContents = false;
		}
	}

	public abstract boolean load(INFORMATION input);

	/**
	 * This implementation delegates this method's return value computation to
	 * {@link #load(Object)}.
	 */
	@Override
	public final boolean hasContents() {
		return this.hasContents;
	}

	@Override
	public Point computeSizeHint() {
		// currently ignores size constraints
		return this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		// return super.computeSizeHint();
	}

	@Override
	public InformationControlCreator<INFORMATION> getInformationPresenterControlCreator() {
		return null;
	}

	public void layout() {
		Rectangle bounds = this.getBounds();
		Point curpos = Display.getCurrent().getCursorLocation();
		this.getShell().layout();
		// this.getShell().pack();
		// TODO korrekt platzieren
		// this.getShell().setLocation(bounds.x, bounds.y + curpos.y);
	}

}