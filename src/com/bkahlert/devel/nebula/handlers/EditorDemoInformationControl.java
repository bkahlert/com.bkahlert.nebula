package com.bkahlert.devel.nebula.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

import com.bkahlert.nebula.information.InformationControl;
import com.bkahlert.nebula.information.InformationControlManagerUtils;

public class EditorDemoInformationControl extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		InformationControl<?> control = InformationControlManagerUtils
				.getCurrentControl();
		Object input = InformationControlManagerUtils.getCurrentInput();
		MessageDialog.openInformation(null, "Control & Input",
				"Current control: " + control.getClass().getSimpleName() + "\n"
						+ control + "\n\nCurrent input: "
						+ input.getClass().getSimpleName() + "\n" + input);
		return null;
	}
}
