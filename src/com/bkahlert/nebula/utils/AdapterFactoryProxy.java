package com.bkahlert.nebula.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;

/**
 * Eclipse's default {@link IAdapterManager} does only consult one
 * {@link IAdapterFactory}. To circumvent this problem, not each
 * {@link IAdapterFactory} registers itself as the adapter but this use a shared
 * instance of this class which redirects the lookup request accordingly.
 */
public class AdapterFactoryProxy<T> implements IAdapterFactory {

	private final Class<T> adaptableClass;
	private final Set<Class<?>> sharedClasses;
	private final Set<IAdapterFactory> adapterFactories;
	private final Map<Class<?>, Set<IAdapterFactory>> adapterToAdapterFactories;

	/**
	 * Constructs a new {@link AdapterFactoryProxy} with the given shared
	 * classes.
	 *
	 * @param adaptableClass
	 *            the class
	 * @param sharedClasses
	 *            if such one is requested all registered
	 *            {@link IAdapterFactory}s are requested until a result !=
	 *            <code>null</code> was found.
	 */
	public AdapterFactoryProxy(Class<T> adaptableClass,
			Class<?>... sharedClasses) {
		this.adaptableClass = adaptableClass;
		this.sharedClasses = Collections.synchronizedSet(new HashSet<Class<?>>(
				Arrays.asList(sharedClasses)));
		this.adapterFactories = Collections
				.synchronizedSet(new HashSet<IAdapterFactory>());
		this.adapterToAdapterFactories = Collections
				.synchronizedMap(new HashMap<Class<?>, Set<IAdapterFactory>>());
	}

	public void registerAdapters(IAdapterFactory adapterFactory) {
		Assert.isNotNull(adapterFactory);
		this.adapterFactories.add(adapterFactory);
		for (Class<?> adapter : adapterFactory.getAdapterList()) {
			if (!this.adapterToAdapterFactories.containsKey(adapter)) {
				this.adapterToAdapterFactories.put(adapter,
						new HashSet<IAdapterFactory>());
			}
			this.adapterToAdapterFactories.get(adapter).add(adapterFactory);
		}
		this.refreshRegistration();
	}

	private void refreshRegistration() {
		Platform.getAdapterManager().unregisterAdapters(this,
				this.adaptableClass);
		Platform.getAdapterManager()
				.registerAdapters(this, this.adaptableClass);
	}

	@Override
	synchronized public Object getAdapter(Object adaptableObject,
			@SuppressWarnings("rawtypes") Class adapterType) {
		Set<IAdapterFactory> adapterFactoriesToAsk = Collections
				.synchronizedSet(new HashSet<IAdapterFactory>());
		if (this.sharedClasses.contains(adapterType)) {
			adapterFactoriesToAsk.addAll(this.adapterFactories);
		} else if (this.adapterToAdapterFactories.containsKey(adapterType)) {
			adapterFactoriesToAsk.addAll(this.adapterToAdapterFactories
					.get(adapterType));
		}

		for (IAdapterFactory adapterFactoryToAsk : adapterFactoriesToAsk) {
			Object adapter = adapterFactoryToAsk.getAdapter(adaptableObject,
					adapterType);
			if (adapterType.isInstance(adapter)) {
				return adapter;
			}
		}

		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		Set<Class<?>> adapterList = new HashSet<Class<?>>();
		adapterList.addAll(this.sharedClasses);
		adapterList.addAll(this.adapterToAdapterFactories.keySet());
		return adapterList.toArray(new Class<?>[adapterList.size()]);
	}

}
