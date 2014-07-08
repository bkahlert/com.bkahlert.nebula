package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class EclipsePreferenceUtil extends AbstractSourceProvider {

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

	@Override
	final public String[] getProvidedSourceNames() {
		Object[] sourceNameObjects = this.getCurrentState().keySet().toArray();
		List<String> sourceNames = new ArrayList<String>(
				sourceNameObjects.length);
		for (Object sourceNameObject : sourceNameObjects) {
			if (sourceNameObject != null) {
				sourceNames.add(sourceNameObject.toString());
			}
		}
		return sourceNames.toArray(new String[sourceNames.size()]);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getCurrentState() {
		return new HashMap<String, Object>();
	}

	@Override
	public void dispose() {
	}

}
