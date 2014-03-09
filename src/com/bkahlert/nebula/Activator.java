package com.bkahlert.nebula;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	private static Activator plugin;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.plugin = this;

		URL confURL = bundleContext.getBundle().getEntry("log4j.xml");
		DOMConfigurator.configure(FileLocator.toFileURL(confURL).getFile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.plugin = null;
		Activator.context = null;
	}

	public static AbstractUIPlugin getDefault() {
		return plugin;
	}

}
