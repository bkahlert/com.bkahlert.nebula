package com.bkahlert.nebula.utils.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import com.bkahlert.nebula.utils.ListUtils;

/**
 * @author bkahlert
 */
public class ArrayUtils {

	/**
	 * Returns a list of elements that are instances of class clazz.
	 *
	 * @param <T>
	 * @param array
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getInstances(Object[] array, Class<T> clazz) {
		if (array == null) {
			return null;
		}

		List<T> instanceObjects = new ArrayList<T>();
		for (Object object : array) {
			if (clazz.isInstance(object)) {
				instanceObjects.add((T) object);
			}
		}

		return instanceObjects;
	}

	/**
	 * Tries to adapt each selection item to adapter and returns all adapted
	 * items.
	 *
	 * @param objects
	 * @param adapter
	 *            to adapt each object to
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <Adapter> List<Adapter> getAdaptableObjects(Object[] objects,
			Class<? extends Adapter> adapter) {
		List<Adapter> adaptableObjects = new ArrayList<Adapter>();

		for (Object object : objects) {
			Adapter adaptedObject = (Adapter) Platform.getAdapterManager()
					.getAdapter(object, adapter);

			if (adaptedObject != null
					&& !adaptableObjects.contains(adaptedObject)) {
				adaptableObjects.add(adaptedObject);
			}
		}

		return adaptableObjects;
	}

	/**
	 * Moves the given position before the given before position and returns the
	 * list itself.
	 *
	 * TODO make more performant
	 *
	 * @param array
	 * @param fromPos
	 * @param beforePos
	 * @return
	 */
	public static <T> T[] moveElement(T[] array, int fromPos, int beforePos) {
		List<T> list = ListUtils.moveElement(
				new ArrayList<T>(Arrays.asList(array)), fromPos, beforePos);
		for (int i = 0, m = list.size(); i < m; i++) {
			array[i] = list.get(i);
		}
		return array;
	}

}
