package com.bkahlert.nebula.utils;

import org.eclipse.core.runtime.Platform;

public class AdapterUtils {
	@SuppressWarnings("unchecked")
	public static <T> T adapt(Object object, Class<T> adaptTo) {
		if (adaptTo.isInstance(object)) {
			return (T) object;
		}
		return (T) Platform.getAdapterManager().getAdapter(object, adaptTo);
	}
}
