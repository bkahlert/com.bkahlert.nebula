package com.bkahlert.nebula.gallery.util.deprecated;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Instances of this class are used in conjunction with {@link ITreeElement}s as
 * generic {@link ITreeContentProvider}s
 * 
 * @author bkahlert
 */
public class TreeContentProvider implements ITreeContentProvider {

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	public void dispose() {
		// do nothing
	}

	public Object[] getElements(Object inputElement) {
		return this.getChildren(inputElement);
	}

	public Object getParent(Object element) {
		return (element instanceof ITreeElement) ? ((ITreeElement) element)
				.getParent() : null;
	}

	public Object[] getChildren(Object parentElement) {
		return (parentElement instanceof ITreeElement) ? ((ITreeElement) parentElement)
				.getChildren() : new Object[0];
	}

	public boolean hasChildren(Object element) {
		return (element instanceof ITreeElement) ? ((ITreeElement) element)
				.hasChildren() : false;
	}

}