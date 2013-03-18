package com.bkahlert.devel.nebula.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

public class EditorDemoInformationControl extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageDialog.openInformation(null, "bkahlert.com Nebula",
				"Hello, Eclipse world");
		return null;
	}
}
