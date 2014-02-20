package com.bkahlert.nebula.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.views.EditorView;

public class ToggleSourceModeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ToggleSourceModeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		boolean sourceMode = !oldValue;
		for (IWorkbenchPage page : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPages()) {
			for (IViewReference viewReference : page.getViewReferences()) {
				IWorkbenchPart part = viewReference.getPart(true);
				if (part instanceof EditorView<?>) {
					EditorView<?> editorView = (EditorView<?>) part;
					if (sourceMode) {
						editorView.getEditor().showSource();
					} else {
						editorView.getEditor().hideSource();
					}
				}
			}
		}
		return null;
	}
}