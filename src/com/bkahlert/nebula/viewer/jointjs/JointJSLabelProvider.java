package com.bkahlert.nebula.viewer.jointjs;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Point;

import com.bkahlert.nebula.utils.colors.RGB;

public abstract class JointJSLabelProvider extends LabelProvider {

	/**
	 * @see returns the same as {@link #getText(Object)}
	 */
	public String getTitle(Object element) {
		return this.getText(element);
	}

	public abstract String getContent(Object element);

	public abstract RGB getColor(Object element);

	public abstract RGB getBackgroundColor(Object element);

	public abstract RGB getBorderColor(Object element);

	public abstract Point getPosition(Object element);

	public abstract Point getSize(Object element);

}
