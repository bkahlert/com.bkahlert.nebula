package com.bkahlert.nebula.viewer.jointjs;

import org.eclipse.jface.viewers.ITreeContentProvider;

public interface JointJSContentProvider extends ITreeContentProvider {

	public String getId(Object element);

	public Object[] getLinks(Object element);

}
