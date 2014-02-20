package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {

	public static interface IStringAdapter<T> {
		public String getString(T object);
	}

	public static String join(List<String> strings, String separator) {
		if (strings == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0, m = strings.size(); i < m; i++) {
			String string = strings.get(i);
			if (string == null)
				string = "";

			sb.append(string);
			if (i + 1 < m)
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Returns the longest prefix of the given strings.
	 * 
	 * @param stringAdapter
	 *            adapts a value object to a string
	 * @param string1
	 * @param string2
	 * 
	 * @return
	 * 
	 * @see <a
	 *      href="http://stackoverflow.com/questions/8033655/java-find-longest-common-prefix-in-java">http://stackoverflow.com/questions/8033655/java-find-longest-common-prefix-in-java</a>
	 */
	public static String getLongestCommonPrefix(String string1, String string2) {
		if (string1 == null || string2 == null)
			throw new IllegalArgumentException();
		int minLength = Math.min(string1.length(), string2.length());
		for (int i = 0; i < minLength; i++) {
			if (string1.charAt(i) != string2.charAt(i)) {
				return string1.substring(0, i);
			}
		}
		return string1.substring(0, minLength);
	}

	/**
	 * Returns the longest prefix of the given strings.
	 * 
	 * @param stringAdapter
	 *            adapts a value object to a string
	 * @param objects
	 * 
	 * @return
	 * 
	 * @see <a
	 *      href="http://stackoverflow.com/questions/1916218/find-the-longest-common-starting-substring-in-a-set-of-strings">Find
	 *      the longest common starting substring in a set of strings</a>
	 */
	public static <T> String getLongestCommonPrefix(
			IStringAdapter<T> stringAdapter, T... objects) {
		if (objects == null)
			throw new IllegalArgumentException();

		List<String> strings = new ArrayList<String>();
		for (T object : objects) {
			if (object == null && stringAdapter == null)
				throw new IllegalArgumentException();
			strings.add(stringAdapter != null ? stringAdapter.getString(object)
					: object.toString());
		}

		if (strings.size() == 0)
			return "";
		if (strings.size() == 1) {
			return strings.get(0);
		}

		try {
			Collections.sort(strings);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(e);
		}

		return getLongestCommonPrefix(strings.get(0),
				strings.get(strings.size() - 1));
	}

	/**
	 * Returns all found common prefixes and their occurrences.
	 * <p>
	 * Prefixes that are not common and thus have occurrence one are not part of
	 * the result.<br/>
	 * Prefixes can only be of length 1 or greater.
	 * <p>
	 * e.g. given AAA, AAB, BBB returns prefix AA with number of occurrences 2.
	 * 
	 * @param stringAdapter
	 *            TODO
	 * @param objects
	 * 
	 * @return
	 */
	public static <T> Map<String, Integer> getLongestCommonPrefix(
			IStringAdapter<T> stringAdapter, int partitionLength, T... objects) {
		if (objects == null)
			throw new IllegalArgumentException();

		Map<String, List<String>> partitionedStrings = new HashMap<String, List<String>>();
		for (T object : objects) {
			if (object == null && stringAdapter == null)
				throw new IllegalArgumentException();
			String string = stringAdapter != null ? stringAdapter
					.getString(object) : object.toString();
			if (string == null)
				throw new IllegalArgumentException();
			if (string.length() < partitionLength)
				continue;
			String key = string.substring(0, partitionLength);
			if (!partitionedStrings.containsKey(key))
				partitionedStrings.put(key, new ArrayList<String>());
			partitionedStrings.get(key).add(string);
		}

		Map<String, Integer> rs = new HashMap<String, Integer>();
		for (List<String> partition : partitionedStrings.values()) {
			String longestPrefix = getLongestCommonPrefix(null,
					partition.toArray());
			rs.put(longestPrefix, partition.size());
		}

		return rs;
	}
}
