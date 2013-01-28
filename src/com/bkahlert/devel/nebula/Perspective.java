package com.bkahlert.devel.nebula;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.bkahlert.devel.nebula.views.WidgetsView;


public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.addView(WidgetsView.ID, IPageLayout.TOP, IPageLayout.RATIO_MAX,
				IPageLayout.ID_EDITOR_AREA);
	}
}
