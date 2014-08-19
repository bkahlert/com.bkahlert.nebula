package com.bkahlert.nebula.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IteratorUtilsTest {

	private static final String[] chars = new String[] { "a", "b", "c" };

	private static IConverter<String, String[]> getChildren = new IConverter<String, String[]>() {
		@Override
		public String[] convert(String returnValue) {
			switch (returnValue.length()) {
			case 0:
			case 1:
			case 2:
				List<String> x = new LinkedList<String>();
				for (String append : chars) {
					x.add(returnValue + append);
				}
				return x.toArray(new String[0]);
			default:
				return null;
			}
		}
	};

	@Test
	public void testBfs() throws Exception {
		int i = 0;
		List<String> expected = Arrays.asList("", "a", "b", "c", "aa", "ab",
				"ac", "ba", "bb", "bc", "ca", "cb", "cc", "aaa", "aab", "aac",
				"aba", "abb", "abc", "aca", "acb", "acc", "baa", "bab", "bac",
				"bba", "bbb", "bbc", "bca", "bcb", "bcc", "caa", "cab", "cac",
				"cba", "cbb", "cbc", "cca", "ccb", "ccc");
		for (Pair<Integer, String> x : IteratorUtils.bfs("", getChildren)) {
			Assert.assertEquals(x.getSecond().length(), x.getFirst().intValue());
			Assert.assertEquals(expected.get(i), x.getSecond());
			i++;
		}
	}

	@Test
	public void testDfs() throws Exception {
		int i = 0;
		List<String> expected = Arrays.asList("", "a", "aa", "aaa", "aab",
				"aac", "ab", "aba", "abb", "abc", "ac", "aca", "acb", "acc",
				"b", "ba", "baa", "bab", "bac", "bb", "bba", "bbb", "bbc",
				"bc", "bca", "bcb", "bcc", "c", "ca", "caa", "cab", "cac",
				"cb", "cba", "cbb", "cbc", "cc", "cca", "ccb", "ccc");
		for (String x : IteratorUtils.dfs("", getChildren)) {
			Assert.assertEquals(expected.get(i), x);
			i++;
		}
	}
}
