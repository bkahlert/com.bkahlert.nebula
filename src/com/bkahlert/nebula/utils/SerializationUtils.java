package com.bkahlert.nebula.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class SerializationUtils {
	public static <T> String serialize(Collection<T> collection,
			IConverter<T, String> converter) {
		Assert.isNotNull(collection);
		Assert.isNotNull(converter);

		List<String> strings = new ArrayList<String>();
		for (Iterator<T> it = collection.iterator(); it.hasNext();) {
			T object = it.next();
			strings.add(converter.convert(object));
		}
		try {
			byte[] pref = org.apache.commons.lang.SerializationUtils
					.serialize((Serializable) strings);
			return new String(pref);
		} catch (Exception e) {
			throw new RuntimeException("Error serializing: " + collection, e);
		}
	}

	public static <T> Collection<T> deserialize(String serialized,
			IConverter<String, T> converter) {
		Assert.isNotNull(serialized);
		Assert.isLegal(!serialized.isEmpty());
		Assert.isNotNull(converter);

		try {
			@SuppressWarnings("unchecked")
			List<String> strings = (List<String>) org.apache.commons.lang.SerializationUtils
					.deserialize(serialized.getBytes());
			List<T> uris = new ArrayList<T>(strings.size());
			for (String string : strings) {
				uris.add(converter.convert(string));
			}
			return uris;
		} catch (Exception e) {
			throw new RuntimeException("Error deserializing: " + serialized, e);
		}
	}
}
