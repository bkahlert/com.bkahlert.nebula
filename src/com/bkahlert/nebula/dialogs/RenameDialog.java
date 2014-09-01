package com.bkahlert.nebula.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RenameDialog extends TitleAreaDialog {

	private Text captionText;

	private String caption;

	public RenameDialog(Shell parentShell, String title) {
		super(parentShell);
		this.caption = title;
	}

	@Override
	public void create() {
		super.create();
		this.setTitle("Rename");
		this.setMessage("Please enter the new caption",
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new FillLayout());

		this.captionText = new Text(container, SWT.BORDER);
		this.captionText.setText(this.caption);
		return area;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private void saveInput() {
		this.caption = this.captionText.getText();
	}

	@Override
	protected void okPressed() {
		this.saveInput();
		super.okPressed();
	}

	public String getCaption() {
		return this.caption;
	}
}