package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

public abstract class InformationControlCreator<INFORMATION>
		extends AbstractReusableInformationControlCreator {

	@Override
	protected abstract InformationControl<INFORMATION> doCreateInformationControl(
			Shell parent);

}
