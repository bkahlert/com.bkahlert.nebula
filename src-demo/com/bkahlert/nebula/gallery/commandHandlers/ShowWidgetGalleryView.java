package com.bkahlert.nebula.gallery.commandHandlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bkahlert.nebula.gallery.views.GalleryView;


public class ShowWidgetGalleryView extends AbstractHandler {

    protected final Logger log = Logger.getLogger(ShowWidgetGalleryView.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {
	try {
	    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		    .getActivePage().showView(GalleryView.ID);
	} catch (PartInitException e) {
	    log.error(
		    "Error showing " + GalleryView.class.getSimpleName(),
		    e);
	}
	return null;
    }

}
