package com.bkahlert.nebula.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.core.runtime.Assert;

public class SerializationUtils {
	public static String serialize(Serializable object) throws IOException {
		String encoded = null;

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(
				byteArrayOutputStream);
		objectOutputStream.writeObject(object);
		objectOutputStream.close();
		encoded = new String(Base64.encodeBase64(byteArrayOutputStream
				.toByteArray()));
		return encoded;
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String string, Class<T> clazz)
			throws IOException, ClassNotFoundException {
		byte[] bytes = Base64.decodeBase64(string.getBytes());
		T object = null;
		ObjectInputStream objectInputStream = new ObjectInputStream(
				new ByteArrayInputStream(bytes));
		object = (T) objectInputStream.readObject();
		return object;
	}

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
			return serialize((Serializable) strings);
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
			List<String> strings = (List<String>) deserialize(serialized,
					Serializable.class);
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
