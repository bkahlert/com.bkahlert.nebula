package com.bkahlert.devel.nebula.utils;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionProviderDelegator implements ISelectionProvider {

	private ISelectionProvider selectionProvider;
	private ListenerList selectionChangedListeners = new ListenerList();
	private ISelection selection = StructuredSelection.EMPTY;

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		this.selectionChangedListeners.add(listener);
		if (this.selectionProvider != null) {
			this.selectionProvider.addSelectionChangedListener(listener);
		}
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (this.selectionChangedListeners != null) {
			this.selectionChangedListeners.remove(listener);
			if (selectionProvider != null) {
				selectionProvider.removeSelectionChangedListener(listener);
			}
		}
	}

	public ISelection getSelection() {
		return this.selectionProvider != null ? this.selectionProvider
				.getSelection() : this.selection;
	}

	public void setSelection(ISelection selection) {
		if (this.selectionProvider != null) {
			this.selectionProvider.setSelection(selection);
		} else {
			this.selection = selection;
			if (this.selectionChangedListeners != null) {
				SelectionChangedEvent event = new SelectionChangedEvent(this,
						selection);
				Object[] listeners = this.selectionChangedListeners
						.getListeners();
				for (Object listener : listeners) {
					((ISelectionChangedListener) listener)
							.selectionChanged(event);
				}
			}

		}
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		if (this.selectionProvider != selectionProvider) {
			ISelection selection = null;
			if (this.selectionChangedListeners != null) {
				Object[] listeners = this.selectionChangedListeners
						.getListeners();

				if (this.selectionProvider != null) {
					for (int i = 0; i < listeners.length; i++) {
						this.selectionProvider
								.removeSelectionChangedListener((ISelectionChangedListener) listeners[i]);
					}
				}

				if (selectionProvider != null) {
					for (int i = 0; i < listeners.length; i++) {
						selectionProvider
								.addSelectionChangedListener((ISelectionChangedListener) listeners[i]);
					}

					selection = selectionProvider.getSelection();
				} else {
					selection = this.selection;
				}
			}
			this.selectionProvider = selectionProvider;
			if (selection != null) {
				this.setSelection(selection);
			}
		}
	}

	public ISelectionProvider getSelectionProvider() {
		return this.selectionProvider;
	}

}
