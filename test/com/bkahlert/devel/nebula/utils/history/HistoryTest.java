package com.bkahlert.devel.nebula.utils.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.bkahlert.devel.nebula.utils.history.History.NoNextElementException;
import com.bkahlert.devel.nebula.utils.history.History.NoPreviousElementException;

public class HistoryTest {

	@Test
	public void test() {
		IHistory<String> history = new History<String>();
		assertEquals(0, history.size());
		assertEquals(-1, history.pos());
		assertNull(history.get());
		assertFalse(history.hasPrev());
		assertFalse(history.hasNext());

		history.add("1");
		assertEquals(0, history.pos());
		assertEquals("1", history.get());
		assertEquals(1, history.size());
		assertFalse(history.hasPrev());
		assertFalse(history.hasNext());

		history.add("2");
		assertEquals(1, history.pos());
		assertEquals("2", history.get());
		assertEquals(2, history.size());
		assertTrue(history.hasPrev());
		assertFalse(history.hasNext());

		history.add("3");
		assertEquals(2, history.pos());
		assertEquals("3", history.get());
		assertEquals(3, history.size());
		assertTrue(history.hasPrev());
		assertFalse(history.hasNext());

		assertEquals("2", history.back());
		assertEquals(1, history.pos());
		assertEquals("2", history.get());
		assertEquals(3, history.size());
		assertTrue(history.hasPrev());
		assertTrue(history.hasNext());

		assertEquals("1", history.back());
		assertEquals(0, history.pos());
		assertEquals("1", history.get());
		assertEquals(3, history.size());
		assertFalse(history.hasPrev());
		assertTrue(history.hasNext());

		assertEquals("2", history.forward());
		assertEquals(1, history.pos());
		assertEquals("2", history.get());
		assertEquals(3, history.size());
		assertTrue(history.hasPrev());
		assertTrue(history.hasNext());

		// element 3 was the next one; will be deleted
		history.add("4");
		assertEquals(2, history.pos());
		assertEquals("4", history.get());
		assertEquals(3, history.size());
		assertTrue(history.hasPrev());
		assertFalse(history.hasNext());

		assertEquals("2", history.back());
		assertEquals(1, history.pos());
		assertEquals("2", history.get());
		assertEquals(3, history.size());
		assertTrue(history.hasPrev());
		assertTrue(history.hasNext());

		assertEquals("1", history.back());
		assertEquals(0, history.pos());
		assertEquals("1", history.get());
		assertEquals(3, history.size());
		assertFalse(history.hasPrev());
		assertTrue(history.hasNext());

		// element 2 and 4 are the next ones; will be deleted
		history.add("5");
		assertEquals(1, history.pos());
		assertEquals("5", history.get());
		assertEquals(2, history.size());
		assertTrue(history.hasPrev());
		assertFalse(history.hasNext());

		history.clear();
		assertEquals(0, history.size());
		assertEquals(-1, history.pos());
		assertNull(history.get());
		assertFalse(history.hasPrev());
		assertFalse(history.hasNext());
	}

	@Test(expected = NoPreviousElementException.class)
	public void testLeftBound() {
		IHistory<String> history = new History<String>();
		assertEquals(0, history.size());
		assertEquals(-1, history.pos());

		history.add("1");
		history.back();
		history.back();
	}

	@Test(expected = NoNextElementException.class)
	public void testRightBound() {
		IHistory<String> history = new History<String>();
		assertEquals(0, history.size());
		assertEquals(-1, history.pos());

		history.add("1");
		history.forward();
	}
}
