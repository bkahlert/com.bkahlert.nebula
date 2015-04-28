package com.bkahlert.nebula.lang;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Assert;

/**
 * This is a special {@link HashMap} that stores {@link Collections}s as its
 * values.
 * <p>
 * It differs in the following way:
 * <ul>
 * <li>Values are never null. If a key points to null, an empty
 * {@link Collection} is returned.</li>
 * <li>The new methods {@link #addTo(Object, Object) and
 *
 * @link #removeFrom(Object, Object)} are provided.</li>
 *       </ul>
 *
 * @author bkahlert
 *
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the each {@link Collection}'s values
 * @param <C>
 *            the type of the each collection
 */
public class CollectionHashMap<K, V, C extends Collection<V>> extends
		HashMap<K, C> {

	private static final long serialVersionUID = 1L;

	private Supplier<C> generator;

	/**
	 * Constructs a new {@link CollectionHashMap} using the given generator.
	 *
	 * @param generator
	 *            must create a new {@link Collection} on each call
	 */
	public CollectionHashMap(Supplier<C> generator) {
		Assert.isLegal(generator != null);
		this.generator = generator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public C get(Object key) {
		C value = super.get(key);
		if (value == null) {
			value = this.generator.get();
			if (value == null) {
				throw new NullPointerException(
						"The generator must never return null!");
			}
			this.put((K) key, value);
			return value;
		} else {
			return value;
		}
	}

	public C addTo(K key, V value) {
		C set = this.get(key);
		set.add(value);
		return set;
	}

	public C addAllTo(K key, Collection<V> values) {
		C set = this.get(key);
		set.addAll(values);
		return set;
	}

	public C removeFrom(K key, V value) {
		C set = this.get(key);
		set.remove(value);
		return set;
	}

}
