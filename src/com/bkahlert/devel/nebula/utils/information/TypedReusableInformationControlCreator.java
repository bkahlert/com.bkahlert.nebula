package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

public abstract class TypedReusableInformationControlCreator<INFORMATION>
		extends AbstractReusableInformationControlCreator {

	@Override
	protected abstract TypedInformationControl<INFORMATION> doCreateInformationControl(
			Shell parent);

}
