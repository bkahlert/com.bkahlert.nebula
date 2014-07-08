package com.bkahlert.nebula.viewer.jointjs;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Point;

import com.bkahlert.nebula.utils.colors.RGB;

public interface JointJSLabelProvider extends ILabelProvider {

	public abstract String getContent(Object element);

	public abstract RGB getColor(Object element);

	public abstract RGB getBackgroundColor(Object element);

	public abstract RGB getBorderColor(Object element);

	public abstract Point getSize(Object element);

}
