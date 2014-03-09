package com.bkahlert.nebula.utils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class EclipsePreferenceUtil {

	private final IPreferenceStore preferenceStore;

	public EclipsePreferenceUtil(AbstractUIPlugin plugin) {
		this.preferenceStore = (plugin != null) ? plugin.getPreferenceStore()
				: null;
	}

	public void addPropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}

	public void removePropertyChangeListener(
			IPropertyChangeListener propertyChangeListener) {
		this.preferenceStore
				.removePropertyChangeListener(propertyChangeListener);
	}

	public IPreferenceStore getPreferenceStore() {
		return this.preferenceStore;
	}

}
