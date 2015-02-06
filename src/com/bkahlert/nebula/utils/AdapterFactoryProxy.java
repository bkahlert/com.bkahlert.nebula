package com.bkahlert.nebula.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.ArrayUtils;
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
	private final Class<?>[] sharedClasses;
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
		this.sharedClasses = sharedClasses;
		this.adapterFactories = new CopyOnWriteArraySet<>();
		this.adapterToAdapterFactories = new ConcurrentHashMap<>();
	}

	synchronized public void registerAdapters(IAdapterFactory adapterFactory) {
		Assert.isNotNull(adapterFactory);
		this.adapterFactories.add(adapterFactory);
		for (Class<?> adapter : adapterFactory.getAdapterList()) {
			if (!this.adapterToAdapterFactories.containsKey(adapter)) {
				this.adapterToAdapterFactories.put(adapter,
						new CopyOnWriteArraySet<IAdapterFactory>());
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
		if (ArrayUtils.contains(this.sharedClasses, adapterType)) {
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
		for (Class<?> sharedClass : this.sharedClasses) {
			adapterList.add(sharedClass);
		}
		adapterList.addAll(this.adapterToAdapterFactories.keySet());
		return adapterList.toArray(new Class<?>[adapterList.size()]);
	}

}
