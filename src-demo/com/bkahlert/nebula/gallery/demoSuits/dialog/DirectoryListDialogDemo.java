package com.bkahlert.nebula.gallery.demoSuits.dialog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.dialogs.DirectoryListDialog;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;

@Demo
public class DirectoryListDialogDemo extends AbstractDemo {

	@Override
	public void createControls(Composite composite) {
		Button openFileListDialog = new Button(composite, SWT.PUSH);
		openFileListDialog.setText("Open File List Dialog");
		openFileListDialog.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryListDialog dialog = new DirectoryListDialog(
						new Shell(), Arrays.asList(new File("abc"), new File(
								"/Users/bkahlert/etc.")));
				dialog.create();
				dialog.setTitle("Data Directories");
				dialog.setText("Add or remove data directories.");
				if (dialog.open() == Window.OK) {
					List<File> directories = dialog.getDirectories();
					System.out.println(directories.size());
				}
			}
		});
	}

}
