package com.bkahlert.devel.nebula.utils.information;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

public interface ITypedInformationControlCreator<INFORMATION> extends
		IInformationControlCreator {
	@Override
	public TypedInformationControl<INFORMATION> createInformationControl(Shell parent);
}
