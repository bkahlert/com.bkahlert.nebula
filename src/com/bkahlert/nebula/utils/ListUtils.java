package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

public class ListUtils {
	/**
	 * Moves the given position before the given before position and returns the
	 * list itself.
	 * 
	 * @param list
	 * @param fromPos
	 * @param beforePos
	 * @return
	 */
	public static <T> List<T> moveElement(List<T> list, int fromPos,
			int beforePos) {
		Assert.isLegal(fromPos >= 0 && fromPos < list.size());
		Assert.isLegal(beforePos >= 0 && beforePos <= list.size());
		T element = list.remove(fromPos);
		if (fromPos < beforePos) {
			beforePos--;
		}
		if (beforePos < 0) {
			beforePos = list.size();
		}
		list.add(beforePos, element);
		return list;
	}

	/**
	 * Reorders the elements contained in the given list to reflect the given
	 * new positions.
	 * 
	 * @param list
	 *            to be reordered
	 * @param translation
	 *            list containing the new indices; a index of <code>null</code>
	 *            means the position stays unchanged
	 * @return
	 * @throws IllegalArgumentException
	 *             if one of the arguments is null or the translation is
	 *             inappropriate (unequal length, missing indices)
	 */
	public static <T> List<T> translate(List<T> list, List<Integer> translation) {
		Assert.isLegal(list != null);
		Assert.isLegal(translation != null);
		Assert.isLegal(translation.size() == list.size());
		for (int i = 0, m = translation.size(); i < m; i++) {
			if (translation.get(i) == null) {
				translation.set(i, i);
			}
		}
		for (int i = 0, m = translation.size(); i < m; i++) {
			Assert.isLegal(translation.contains(i));
		}

		List<T> flush = new ArrayList<T>(list);
		for (int i = 0, m = flush.size(); i < m; i++) {
			int newI = translation.get(i) != null ? translation.get(i) : i;
			list.set(newI, flush.get(i));
		}
		return list;
	}
}
