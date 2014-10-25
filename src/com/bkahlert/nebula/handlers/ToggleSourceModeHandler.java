package com.bkahlert.nebula.handlers;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.bkahlert.nebula.utils.WorkbenchUtils;
import com.bkahlert.nebula.views.EditorView;
import com.bkahlert.nebula.widgets.editor.Editor;

public class ToggleSourceModeHandler extends AbstractHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(ToggleSourceModeHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean oldValue = HandlerUtil.toggleCommandState(command);
		boolean sourceMode = !oldValue;
		for (EditorView<?> editorView : WorkbenchUtils
				.getViews(EditorView.class)) {
			if (sourceMode) {
				for (Editor<?> editor : editorView.getEditors()) {
					editor.showSource();
				}
			} else {
				for (Editor<?> editor : editorView.getEditors()) {
					editor.hideSource();
				}
			}
		}
		return null;
	}
}