package com.bkahlert.devel.nebula.dialogs;

import java.awt.MouseInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class PopupDialog extends org.eclipse.jface.dialogs.PopupDialog {

	public PopupDialog(Shell parent, int shellStyle, boolean takeFocusOnOpen,
			boolean persistSize, boolean persistLocation,
			boolean showDialogMenu, boolean showPersistActions,
			String titleText, String infoText) {
		super(parent, shellStyle, takeFocusOnOpen, persistSize,
				persistLocation, showDialogMenu, showPersistActions, titleText,
				infoText);
	}

	public PopupDialog(String titleText, String infoText) {
		super(null, SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP, false, false,
				false, false, false, titleText, infoText);
	}

	public PopupDialog() {
		super(null, SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP, false, false,
				false, false, false, null, null);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		int offset = 5;
		int x = MouseInfo.getPointerInfo().getLocation().x - initialSize.x
				- offset;
		int y = MouseInfo.getPointerInfo().getLocation().y - initialSize.y
				- offset;
		return new Point(x, y);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		this.createControls(composite);
		return composite;
	}

	protected abstract Control createControls(Composite parent);

}
