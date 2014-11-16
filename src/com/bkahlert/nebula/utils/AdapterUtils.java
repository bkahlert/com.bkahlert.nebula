package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Platform;

public class AdapterUtils {
	@SuppressWarnings("unchecked")
	public static <T> T adapt(Object object, Class<T> adaptTo) {
		if (adaptTo.isInstance(object)) {
			return (T) object;
		}
		return (T) Platform.getAdapterManager().getAdapter(object, adaptTo);
	}

	public static <T> List<T> adaptAll(Collection<?> objects, Class<T> adaptTo) {
		List<T> adaptions = new ArrayList<T>();
		for (Object object : objects) {
			adaptions.add(adapt(object, adaptTo));
		}
		return adaptions;
	}
}
