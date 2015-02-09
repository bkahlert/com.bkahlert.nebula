package com.bkahlert.nebula.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This is a special {@link HashMap} that stores {@link Set}s as its values. See
 * {@link CollectionHashMap} for more details.
 *
 * @author bkahlert
 *
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the each {@link Set}'s values
 */
public class SetHashMap<K, V> extends CollectionHashMap<K, V, Set<V>> {

	private static final long serialVersionUID = 1L;

	public SetHashMap() {
		this((Supplier<Set<V>>) null);
	}

	public SetHashMap(Supplier<Set<V>> generator) {
		super(generator != null ? generator : HashSet<V>::new);
	}

}
