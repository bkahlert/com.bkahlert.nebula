package com.bkahlert.nebula.viewer;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;

public abstract class StyledLabelProvider extends ColumnLabelProvider implements
		IStyledLabelProvider {

	@Override
	public String getText(Object element) {
		StyledString s = this.getStyledText(element);
		return s != null ? s.getString() : null;
	}

}