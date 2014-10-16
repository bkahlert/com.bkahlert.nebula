package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ListUtilsTest {

	private final List<String> list = Collections
			.unmodifiableList(new ArrayList<String>(Arrays.asList("0", "1",
					"2", null, "4", "5")));

	private void equals(List<?> list1, List<?> list2) {
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEquals(list1.get(i), list2.get(i));
		}
	}

	private <T> List<T> clone(List<T> list) {
		List<T> clone = new ArrayList<T>();
		for (T item : list) {
			clone.add(item);
		}
		return clone;
	}

	private List<String> list() {
		return this.clone(this.list);
	}

	@Test
	public void testInternals() {
		this.equals(this.list, this.clone(this.list));
		this.equals(this.list, this.list());
	}

	@Test
	public void testTranslate() {
		this.equals(Arrays.asList("0", "1", "2", null, "4", "5"), ListUtils
				.translate(this.list(), Arrays.asList((Integer) null, null,
						null, null, null, null)));
		this.equals(
				Arrays.asList("0", "1", "2", null, "4", "5"),
				ListUtils.translate(this.list(),
						Arrays.asList(0, 1, 2, 3, 4, 5)));

		this.equals(
				Arrays.asList("0", null, "2", "1", "4", "5"),
				ListUtils.translate(this.list(),
						Arrays.asList(0, 3, 2, 1, 4, 5)));

		this.equals(
				Arrays.asList("0", "1", "5", "4", null, "2"),
				ListUtils.translate(this.list(),
						Arrays.asList(null, null, 5, 4, 3, 2)));
	}

	@Test
	public void testTranslateIllegal() {
		try {
			this.equals(
					Arrays.asList("0", "1", "5", "4", null, "2"),
					ListUtils.translate(this.list(),
							Arrays.asList(null, null, 5, 4, 3, 2, null, null)));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(
					Arrays.asList("0", "1", "5", "4", null, "2"),
					ListUtils.translate(this.list(),
							Arrays.asList(null, null, 5, 4, 3, 3)));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(
					Arrays.asList("0", "1", "5", "4", null, "2"),
					ListUtils.translate(this.list(),
							Arrays.asList(null, null, 5, 4, 3)));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(
					Arrays.asList("0", "1", "5", "4", null, "2"),
					ListUtils.translate(null,
							Arrays.asList(null, null, 5, 4, 3)));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(Arrays.asList("0", "1", "5", "4", null, "2"),
					ListUtils.translate(this.list(), null));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testMoveElement() {
		this.equals(Arrays.asList("0", "1", "2", null, "4", "5"),
				ListUtils.moveElement(this.list(), 0, 0));
		this.equals(Arrays.asList("0", "1", "2", null, "4", "5"),
				ListUtils.moveElement(this.list(), 3, 3));

		this.equals(Arrays.asList("0", "1", "2", null, "4", "5"),
				ListUtils.moveElement(this.list(), 0, 1));
		this.equals(Arrays.asList("1", "0", "2", null, "4", "5"),
				ListUtils.moveElement(this.list(), 1, 0));

		this.equals(Arrays.asList("0", "2", "1", null, "4", "5"),
				ListUtils.moveElement(this.list(), 1, 3));
		this.equals(Arrays.asList("0", "2", null, "1", "4", "5"),
				ListUtils.moveElement(this.list(), 1, 4));

		this.equals(Arrays.asList("0", null, "1", "2", "4", "5"),
				ListUtils.moveElement(this.list(), 3, 1));
		this.equals(Arrays.asList("0", "1", null, "2", "4", "5"),
				ListUtils.moveElement(this.list(), 3, 2));

		this.equals(Arrays.asList("4", "0", "1", "2", null, "5"),
				ListUtils.moveElement(this.list(), 4, 0));
		this.equals(Arrays.asList("0", "1", "2", null, "5", "4"),
				ListUtils.moveElement(this.list(), 4, 6));

		this.equals(Arrays.asList("5", "0", "1", "2", null, "4"),
				ListUtils.moveElement(this.list(), 5, 0));
		this.equals(Arrays.asList("0", "1", "2", null, "4", "5"),
				ListUtils.moveElement(this.list(), 5, 6));
	}

	@Test
	public void testMoveElementIllegal() {
		try {
			this.equals(Arrays.asList("0", null, "1", "2", "4", "5"),
					ListUtils.moveElement(this.list(), -1, 1));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(Arrays.asList("0", null, "1", "2", "4", "5"),
					ListUtils.moveElement(this.list(), 6, 1));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(Arrays.asList("0", null, "1", "2", "4", "5"),
					ListUtils.moveElement(this.list(), 3, -1));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			this.equals(Arrays.asList("0", null, "1", "2", "4", "5"),
					ListUtils.moveElement(this.list(), 3, 7));
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

}
