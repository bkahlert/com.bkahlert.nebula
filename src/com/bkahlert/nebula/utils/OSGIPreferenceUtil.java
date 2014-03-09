package com.bkahlert.nebula.utils;

import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

public class OSGIPreferenceUtil {

	private final BundleContext bundleContext;

	private PreferencesService getPreferencesService() {
		return this.bundleContext.getService(this.bundleContext
				.getServiceReference(PreferencesService.class));
	}

	protected Preferences getSystemPreferences() {
		return this.getPreferencesService().getSystemPreferences();
	}

	protected Preferences getUserPreferences(String name) {
		return this.getPreferencesService().getUserPreferences(name);
	}

	public OSGIPreferenceUtil(BundleContext bundleContext) {
		assert bundleContext != null;
		this.bundleContext = bundleContext;
	}

}
