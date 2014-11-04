package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.junit.Test;

public class ViewerUtilsTest {

	@Test
	public void testCreateCompletedTreePath() {
		try {
			assertEquals(0, ViewerUtils.createCompletedTreePath(null).length);
			assertTrue(false);
		} catch (AssertionFailedException e) {

		}

		assertEquals(0, ViewerUtils.createCompletedTreePath(new TreePath(
				new Object[] {})).length);

		TreePath[] completedtreePaths = ViewerUtils
				.createCompletedTreePath(new TreePath(new Object[] { "a", "b",
						"c", "d" }));
		assertEquals(4, completedtreePaths.length);
		assertEquals(new TreePath(new Object[] { "a" }), completedtreePaths[0]);
		assertEquals(new TreePath(new Object[] { "a", "b" }),
				completedtreePaths[1]);
		assertEquals(new TreePath(new Object[] { "a", "b", "c" }),
				completedtreePaths[2]);
		assertEquals(new TreePath(new Object[] { "a", "b", "c", "d" }),
				completedtreePaths[3]);
	}

	@Test
	public void testCreateCompletedTreePaths() {
		try {
			assertEquals(
					0,
					ViewerUtils.createCompletedTreePaths((TreePath[]) null).length);
			assertTrue(false);
		} catch (AssertionFailedException e) {

		}

		try {
			assertEquals(
					0,
					ViewerUtils.createCompletedTreePaths((TreePath) null).length);
			assertTrue(false);
		} catch (AssertionFailedException e) {

		}

		try {
			assertEquals(0, ViewerUtils.createCompletedTreePaths(new TreePath(
					new Object[0]), (TreePath) null).length);
			assertTrue(false);
		} catch (AssertionFailedException e) {

		}

		assertEquals(0, ViewerUtils.createCompletedTreePaths(new TreePath(
				new Object[] {})).length);

		assertEquals(0, ViewerUtils.createCompletedTreePaths(new TreePath(
				new Object[] {}), new TreePath(new Object[] {})).length);

		TreePath[] completedTreePaths = ViewerUtils.createCompletedTreePaths(
				new TreePath(new Object[] { "a", "b", "c", "d" }),
				new TreePath(new Object[] { "a", "e", "f" }));
		assertEquals(6, completedTreePaths.length);
		assertEquals(new TreePath(new Object[] { "a" }), completedTreePaths[0]);
		assertEquals(new TreePath(new Object[] { "a", "b" }),
				completedTreePaths[1]);
		assertEquals(new TreePath(new Object[] { "a", "b", "c" }),
				completedTreePaths[2]);
		assertEquals(new TreePath(new Object[] { "a", "b", "c", "d" }),
				completedTreePaths[3]);
		assertEquals(new TreePath(new Object[] { "a", "e" }),
				completedTreePaths[4]);
		assertEquals(new TreePath(new Object[] { "a", "e", "f" }),
				completedTreePaths[5]);

		try {
			assertEquals(
					0,
					ViewerUtils.createCompletedTreePaths((ITreeSelection) null).length);
			assertTrue(false);
		} catch (AssertionFailedException e) {

		}

		TreePath[] completedParentTreePaths = ViewerUtils
				.createCompletedTreePaths(new TreeSelection(new TreePath[] {
						new TreePath(new Object[] { "a", "b", "c", "d" }),
						new TreePath(new Object[] { "a", "e", "f" }) }));
		assertEquals(4, completedParentTreePaths.length);
		assertEquals(new TreePath(new Object[] { "a" }),
				completedParentTreePaths[0]);
		assertEquals(new TreePath(new Object[] { "a", "b" }),
				completedParentTreePaths[1]);
		assertEquals(new TreePath(new Object[] { "a", "b", "c" }),
				completedParentTreePaths[2]);
		assertEquals(new TreePath(new Object[] { "a", "e" }),
				completedParentTreePaths[3]);
	}

}
