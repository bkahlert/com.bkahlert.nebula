package com.bkahlert.nebula.gallery.application;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		{
			IFolderLayout folderLayout = layout.createFolder("folder",
					IPageLayout.TOP, 0.95f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("com.bkahlert.nebula.gallery.views.GalleryView");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.SampleEditorView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1",
					IPageLayout.RIGHT, 0.75f, "folder");
			folderLayout.addView("org.eclipse.swt.tools.views.SpyView");
		}
	}
}
