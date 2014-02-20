package com.bkahlert.nebula.viewer;

import java.io.File;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class FileLabelProvider extends ColumnLabelProvider {
	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
		if (!(cell.getElement() instanceof File))
			return;

		File file = (File) cell.getElement();

		ISharedImages sharedImages;
		switch (cell.getColumnIndex()) {
		case 0:
			cell.setText(file.toString());

			sharedImages = PlatformUI.getWorkbench().getSharedImages();
			if (file.isDirectory()) {
				cell.setImage(sharedImages
						.getImage(ISharedImages.IMG_OBJ_FOLDER));
			} else if (file.isFile()) {
				cell.setImage(sharedImages
						.getImage(ISharedImages.IMG_OBJ_ELEMENT));
			}
			break;
		}
	}
}
