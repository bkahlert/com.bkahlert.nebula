package com.bkahlert.devel.nebula.dialogs;

import java.awt.MouseInfo;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite;
import com.bkahlert.devel.nebula.widgets.SimpleIllustratedComposite.IllustratedText;

public abstract class PopupDialog extends org.eclipse.jface.dialogs.PopupDialog {

	private IllustratedText titleIllustratedText = null;

	public PopupDialog(Shell parent, int shellStyle, boolean takeFocusOnOpen,
			boolean persistSize, boolean persistLocation,
			boolean showDialogMenu, boolean showPersistActions,
			String titleText, String infoText) {
		super(parent, shellStyle, takeFocusOnOpen, persistSize,
				persistLocation, showDialogMenu, showPersistActions, titleText,
				infoText);
	}

	public PopupDialog(IllustratedText title, String infoText) {
		super(null, INFOPOPUPRESIZE_SHELLSTYLE, false, false, false, false,
				false, null, infoText);
		this.titleIllustratedText = title;
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
	protected boolean hasTitleArea() {
		return this.titleIllustratedText != null;
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		SimpleIllustratedComposite simpleIllustratedComposite = new SimpleIllustratedComposite(
				parent, SWT.NONE, this.titleIllustratedText);
		simpleIllustratedComposite.setSpacing(3);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1)
				.applyTo(simpleIllustratedComposite);

		return simpleIllustratedComposite;
	}

	@Override
	protected Color getBackground() {
		return super.getBackground();
	}

	private Composite composite;

	@SuppressWarnings("rawtypes")
	@Override
	protected List getBackgroundColorExclusions() {
		return Arrays.asList(this.composite);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		this.composite.setLayout(new FillLayout());
		this.createControls(this.composite);
		return this.composite;
	}

	protected abstract Control createControls(Composite composite);

}
