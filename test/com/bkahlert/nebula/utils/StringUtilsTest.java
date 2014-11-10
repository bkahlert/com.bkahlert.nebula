package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;

public class StringUtilsTest {
	@Test
	public void testJoin() {
		assertEquals("a", StringUtils.join(Arrays.asList("a"), ", "));
		assertEquals("a, b", StringUtils.join(Arrays.asList("a", "b"), ", "));
		assertEquals("a, b, c",
				StringUtils.join(Arrays.asList("a", "b", "c"), ", "));

		assertEquals("", StringUtils.join(null, ", "));

		assertEquals("", StringUtils.join(Arrays.asList((String) null), ", "));
		assertEquals(", b",
				StringUtils.join(Arrays.asList((String) null, "b"), ", "));
		assertEquals(
				", , c",
				StringUtils.join(
						Arrays.asList((String) null, (String) null, "c"), ", "));
		assertEquals(
				", b, ",
				StringUtils.join(
						Arrays.asList((String) null, "b", (String) null), ", "));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixesNull1() {
		StringUtils.getLongestCommonPrefix(null, null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixesNull2() {
		StringUtils.getLongestCommonPrefix(null, null, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixesNull3() {
		StringUtils.getLongestCommonPrefix(null, "", null);
	}

	@Test
	public void testLongestCommonPrefix() {
		assertEquals("", StringUtils.getLongestCommonPrefix(null, "", ""));
		assertEquals("", StringUtils.getLongestCommonPrefix(null, "A", ""));
		assertEquals("A", StringUtils.getLongestCommonPrefix(null, "A", "A"));
		assertEquals("A", StringUtils.getLongestCommonPrefix(null, "A", "ABBB"));
		assertEquals("AB",
				StringUtils.getLongestCommonPrefix(null, "AB", "ABBB"));
		assertEquals("", StringUtils.getLongestCommonPrefix(null, "AB", "BBBB"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixes1Null1() {
		StringUtils.getLongestCommonPrefix(null, (String[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixes1Null2() {
		StringUtils.getLongestCommonPrefix(null, new String[] { "A", null });
	}

	@Test
	public void testGetLongestCommonPrefix1() {
		assertEquals(0, StringUtils.getLongestCommonPrefix(null, new String[0])
				.length());
		assertEquals(0,
				StringUtils.getLongestCommonPrefix(null, new String[] { "" })
				.length());
		assertEquals(1,
				StringUtils.getLongestCommonPrefix(null, new String[] { "A" })
				.length());
		assertEquals(
				0,
				StringUtils.getLongestCommonPrefix(null,
						new String[] { "A", "B" }).length());
		assertEquals(
				0,
				StringUtils.getLongestCommonPrefix(null,
						new String[] { "AB", "BA" }).length());
		assertEquals(
				0,
				StringUtils.getLongestCommonPrefix(null,
						new String[] { "", "" }).length());
		assertEquals(
				4,
				StringUtils.getLongestCommonPrefix(null,
						new String[] { "AAAAA", "AAAAA", "AAAAB" }).length());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixes2Null1() {
		StringUtils.getLongestCommonPrefix(null, 1, (String[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCommonPrefixes2Null2() {
		StringUtils.getLongestCommonPrefix(null, 1, new String[] { "A", null });
	}

	@Test
	public void testGetLongestCommonPrefix2() {
		assertEquals(0,
				StringUtils.getLongestCommonPrefix(null, 1, new String[0])
				.keySet().size());
		assertEquals(0,
				StringUtils
				.getLongestCommonPrefix(null, 1, new String[] { "" })
						.keySet().size());
		assertEquals(
				1,
				StringUtils
				.getLongestCommonPrefix(null, 1, new String[] { "A" })
						.keySet().size());
		assertEquals(
				2,
				StringUtils
						.getLongestCommonPrefix(null, 1,
						new String[] { "A", "B" }).keySet().size());
		assertEquals(
				1,
				StringUtils
						.getLongestCommonPrefix(null, 1,
						new String[] { "AB", "AA" }).keySet().size());
		assertEquals(
				0,
				StringUtils
				.getLongestCommonPrefix(null, 1,
						new String[] { "", "" }).keySet().size());

		Map<String, Integer> rs = StringUtils.getLongestCommonPrefix(null, 1,
				new String[] { "AAAAA", "AAAAA", "AAAAB", "AAA", "BBBBBB",
						"BBBBC", "C", "CC", "CCC", "D" });
		assertEquals(4, rs.keySet().size());
		for (int i = 0; i < rs.keySet().size(); i++) {
			if (rs.containsKey("AAA")) {
				assertEquals(4, (int) rs.get("AAA"));
				rs.remove("AAA");
			}
			if (rs.containsKey("BBBB")) {
				assertEquals(2, (int) rs.get("BBBB"));
				rs.remove("BBBB");
			}
			if (rs.containsKey("C")) {
				assertEquals(3, (int) rs.get("C"));
				rs.remove("C");
			}
			if (rs.containsKey("D")) {
				assertEquals(1, (int) rs.get("D"));
				rs.remove("D");
			}
		}
		assertEquals(0, rs.keySet().size());
	}

	@Test
	public void testGetRandomString() {
		Pattern pattern = Pattern.compile("^[a-z0-9]*$");
		for (int i = 0; i < 1000; i++) {
			String str = StringUtils.createRandomString(i);
			assertTrue(pattern.matcher(str).matches());
			assertEquals(i, str.length());
		}
	}
}
