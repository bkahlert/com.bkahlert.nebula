package com.bkahlert.nebula.gallery.application;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = this.getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 700));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);
		configurer.setTitle("bkahlert.com - Nebula"); //$NON-NLS-1$
	}
}
