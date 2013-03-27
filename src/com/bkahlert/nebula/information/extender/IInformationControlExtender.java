package com.bkahlert.nebula.information.extender;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.information.InformationControl;

/**
 * Instances of this class can extend {@link InformationControl}s by extending
 * the extension point <code>com.bkahlert.nebula.information</code>.
 * 
 * @author bkahlert
 * 
 */
public interface IInformationControlExtender<INFORMATION> {
	public void extend(InformationControl<INFORMATION> informationControl,
			Composite parent);

	public void extend(InformationControl<INFORMATION> informationControl,
			INFORMATION information);
}
