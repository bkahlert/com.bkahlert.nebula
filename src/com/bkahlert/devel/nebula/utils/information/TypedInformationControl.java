package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * This is a typed version of the {@link IInformationControlExtension2}.<br>
 * Instead of having to override
 * {@link IInformationControlExtension2#setInput(Object)} you have to override
 * {@link #setTypedInput(Object)} that returns a boolean whether to show
 * information or not.
 * 
 * @author bkahlert
 * 
 * @param <INFORMATION>
 */
public abstract class TypedInformationControl<INFORMATION> extends
		AbstractInformationControl implements IInformationControlExtension2 {

	private boolean hasContents = false;

	public TypedInformationControl(Shell parentShell, boolean resizable) {
		super(parentShell, resizable);
		this.create();
	}

	@Override
	protected abstract void createContent(Composite parent);

	@SuppressWarnings("unchecked")
	@Override
	public final void setInput(Object input) {
		try {
			this.hasContents = this.setTypedInput((INFORMATION) input);
		} catch (ClassCastException e) {
			this.hasContents = false;
		}
	}

	public abstract boolean setTypedInput(INFORMATION input);

	/**
	 * This implementation delegates this method's return value computation to
	 * {@link #setTypedInput(Object)}.
	 */
	@Override
	public final boolean hasContents() {
		return this.hasContents;
	}

	@Override
	public Point computeSizeHint() {
		// currently ignores size constraints
		return this.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

}