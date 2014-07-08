package com.bkahlert.nebula.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.bkahlert.nebula.utils.selection.SelectionUtils;
import com.bkahlert.nebula.viewer.FileListViewer;

public class DirectoryListDialog extends TitleAreaDialog {

	private FileListViewer fileListViewer;
	private List<File> directories;
	private List<File> selectedDirectories;

	public DirectoryListDialog(Shell parentShell, List<File> directories) {
		super(parentShell);
		this.directories = new ArrayList<File>(directories);
	}

	@Override
	public void create() {
		super.create();
	}

	@Override
	public void setTitle(String newTitle) {
		Shell shell = this.getShell();
		if (shell != null)
			shell.setText(newTitle);
	}

	public void setText(String newText) {
		super.setTitle(newText);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Table table = new Table(parent, SWT.BORDER);
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
				.span(1, 2).create());
		fileListViewer = new FileListViewer(table);
		updateViewer();

		Button addButton = new Button(parent, SWT.BORDER);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addButton.setText("Add...");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(
						DirectoryListDialog.this.getShell());
				directoryDialog.setFilterPath(null);
				directoryDialog.setText("Add Data Directory");
				directoryDialog
						.setMessage("Please select the data directory you want to add.");
				String dataDirectoryString = directoryDialog.open();
				if (dataDirectoryString != null) {
					directories.add(new File(dataDirectoryString));
					updateViewer();
				}
			}
		});

		Button removeButton = new Button(parent, SWT.BORDER);
		removeButton
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<File> files = SelectionUtils.getAdaptableObjects(
						fileListViewer.getSelection(), File.class);
				for (File file : files) {
					for (Iterator<File> iterator = directories.iterator();; iterator
							.hasNext()) {
						File directory = iterator.next();
						if (directory.equals(file)) {
							iterator.remove();
							break;
						}
					}
				}
				updateViewer();
			}
		});

		return parent;
	}

	public void updateViewer() {
		fileListViewer.setFiles(this.directories.toArray(new File[0]));
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		control.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.RIGHT, SWT.BOTTOM).span(2, 1).create());
		return control;
	}

	@Override
	protected void okPressed() {
		this.selectedDirectories = SelectionUtils.getAdaptableObjects(
				this.fileListViewer.getSelection(), File.class);
		super.okPressed();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public List<File> getDirectories() {
		return this.directories;
	}

	public List<File> getSelectedDirectories() {
		return selectedDirectories;
	}
}
