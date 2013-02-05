package com.bkahlert.devel.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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
}
