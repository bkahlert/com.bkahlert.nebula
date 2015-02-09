package com.bkahlert.nebula.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is a special {@link HashMap} that stores {@link List}s as its values.
 * See {@link CollectionHashMap} for more details.
 *
 * @author bkahlert
 *
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the each {@link List}'s values
 */
public class ListHashMap<K, V> extends CollectionHashMap<K, V, List<V>> {

	private static final long serialVersionUID = 1L;

	public ListHashMap() {
		this((Supplier<List<V>>) null);
	}

	public ListHashMap(Supplier<List<V>> generator) {
		super(generator != null ? generator : ArrayList<V>::new);
	}

}
