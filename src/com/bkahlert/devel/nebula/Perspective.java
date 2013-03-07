package com.bkahlert.devel.nebula;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		{
			IFolderLayout folderLayout = layout.createFolder("folder",
					IPageLayout.TOP, 0.95f, IPageLayout.ID_EDITOR_AREA);
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.WidgetsView");
			folderLayout
					.addView("de.fu_berlin.imp.seqan.usability_analyzer.nebula.ui.views.SampleEditorView");
		}
	}
}
