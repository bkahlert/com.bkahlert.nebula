package com.bkahlert.nebula.viewer;

import java.io.File;

import org.eclipse.swt.widgets.Table;

public class FileListViewer extends SortableTableViewer {

	File[] displayedFiles = null;

	public FileListViewer(Table table) {
		super(table);
		this.setLabelProvider(new FileLabelProvider());
	}

	public void setFiles(File[] files) {
		if (displayedFiles != null && displayedFiles.length > 0) {
			for (File displayedFile : displayedFiles) {
				this.remove(displayedFile);
			}
		}

		if (files != null) {
			for (File file : files) {
				this.add(file);
			}
		}

		this.displayedFiles = files;
	}

}
