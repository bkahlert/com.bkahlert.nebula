package com.bkahlert.devel.nebula.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.bkahlert.devel.nebula.data.TreeNode;

/**
 * Tests the <code>TreeNode</code> object. It is a generic tree (integer.e. not
 * a balanced tree or some other specialized tree).
 * 
 * @see com.bkahlert.devel.nebula.data.TreeNode
 */
public class TreeNodeTest extends TestCase {

	@SuppressWarnings("unchecked")
	public static TreeNode<Integer> getTree() {
		return new TreeNode<Integer>(17, Arrays.asList(
				new TreeNode<Integer>(1),
				new TreeNode<Integer>(8, Arrays.asList(
						new TreeNode<Integer>(2),
						new TreeNode<Integer>(7, Arrays.asList(
								new TreeNode<Integer>(5, Arrays.asList(
										new TreeNode<Integer>(3),
										new TreeNode<Integer>(4))),
								new TreeNode<Integer>(6))))),
				new TreeNode<Integer>(16, Arrays.asList(
						new TreeNode<Integer>(11, Arrays.asList(
								new TreeNode<Integer>(9),
								new TreeNode<Integer>(10))),
						new TreeNode<Integer>(12),
						new TreeNode<Integer>(15, Arrays.asList(
								new TreeNode<Integer>(13),
								new TreeNode<Integer>(14)))))));
	}

	/**
	 * Tests the tree nodes defend themselves again invalid parameters that
	 * would be the result of programming errors.
	 */
	@Test
	public void testIllegalOperations() {
		// TimelineViewer cannot add a child node to position greater than available
		// number of children.
		boolean caughtException = false;

		try {
			TreeNode<Integer> root = new TreeNode<Integer>();
			root.add(new TreeNode<Integer>(), 1);
		} catch (IllegalArgumentException e) {
			caughtException = true;
		} catch (Throwable t) {
			String actualExceptionName = t.getClass().getName();
			fail("Expected exception 'IllegalArgumentException' and got '"
					+ actualExceptionName + "'.");
		}

		if (caughtException == false) {
			fail("Expected exception 'IllegalArgumentException' but no exceptions caught.");
		}

		// TimelineViewer remove a child node from position greater than available number
		// of children.
		caughtException = false;

		try {
			TreeNode<Integer> root = new TreeNode<Integer>();
			root.remove(1);
		} catch (IllegalArgumentException e) {
			caughtException = true;
		} catch (Throwable t) {
			String actualExceptionName = t.getClass().getName();
			fail("Expected exception 'IllegalArgumentException' and got '"
					+ actualExceptionName + "'.");
		}

		if (caughtException == false) {
			fail("Expected exception 'IllegalArgumentException' but no exceptions caught.");
		}
	}

	/**
	 * Tests that user objects can be attached and extracted from a tree node.
	 */
	@Test
	public void testUserObjectsStr() {
		String user = "This is a test string.\n  It should go in and come out the same.";
		String user2 = "That's a second test string.";

		// Create a node with the user defined object.
		TreeNode<String> node = new TreeNode<String>(user);

		// Get the object back out.
		String result = node.getData();
		assertEquals("The attached string should be the same one as put in.",
				user, result);

		// Try erasing the string.
		node.setData(null);
		result = node.getData();
		assertNull("No user object should be attached to the node.", result);

		// Put a different object back in and pull it out.
		node.setData(user2);
		result = node.getData();
		assertEquals("The attached user should be the same one as put in.",
				user2, result);
	}

	/**
	 * Tests that user objects can be attached and extracted from a tree node.
	 */
	@Test
	public void testUserObjectsInt() {
		Integer user = new Integer(42);
		Integer user2 = new Integer(13);

		// Create a node with the user defined object.
		TreeNode<Integer> node = new TreeNode<Integer>(user);

		// Get the object back out.
		Integer result = node.getData();
		assertEquals("The attached int should be the same one as put in.",
				user, result);

		// Try erasing the string.
		node.setData(null);
		result = node.getData();
		assertNull("No user object should be attached to the node.", result);

		// Put a different object back in and pull it out.
		node.setData(user2);
		result = node.getData();
		assertEquals("The attached Integer should be the same one as put in.",
				user2, result);
	}

	/**
	 * Tests assertions about a root node. This is a very simple test and should
	 * be right before working with children.
	 */
	@Test
	public void testRoot() {
		TreeNode<?> root = new TreeNode<Object>();

		// Verify properties of the root node.
		boolean isRoot = root.isRoot();
		assertTrue("The node should be the root node.", isRoot);

		int index = root.index();
		assertEquals(
				"The root node should have an index of -1 since it has no parent.",
				-1, index);

		int depth = root.depth();
		assertEquals(
				"The root node should have depth of 0 since it has no parent.",
				0, depth);

		TreeNode<?> parent = root.getParent();
		assertNull("The root node should not have a parent node.", parent);

		boolean hasChildren = root.hasChildren();
		assertTrue(
				"The root node should not have any children because none have been added.",
				hasChildren == false);

		TreeNode<?>[] children = root.children();
		int childLength = children.length;
		assertEquals(
				"The root node should not have any children because none have been added.",
				0, childLength);

		// Verify the following method doesn't throw an exception.
		root.removeFromParent();
	}

	/**
	 * TimelineViewer a tree that has a root and just one child. This is a simple test to
	 * verify the child has properties appropriately set before testing more
	 * complex trees.
	 */
	@Test
	public void testOneChild() {
		// Create the tree.
		TreeNode<Object> root = new TreeNode<Object>();
		TreeNode<Object> child = new TreeNode<Object>();
		root.add(child);

		// Verify properties of the child node.
		boolean isRoot = child.isRoot();
		assertTrue("The node should not be the root node.", isRoot == false);

		int index = child.index();
		assertEquals(
				"The child node should have an index of 0 since it is the only child.",
				0, index);

		int depth = child.depth();
		assertEquals(
				"The child node should have depth of 1 since it is a first level node.",
				1, depth);

		TreeNode<Object> parent = child.getParent();
		assertEquals("The child's parent should be the root node.", root,
				parent);

		boolean hasChildren = child.hasChildren();
		assertTrue(
				"The child node should not have any children because none have been added.",
				hasChildren == false);

		hasChildren = root.hasChildren();
		assertTrue("The root node should now have children.",
				hasChildren == true);

		TreeNode<Object>[] children = child.children();
		int childLength = children.length;
		assertEquals(
				"The child node should not have any children because none have been added.",
				0, childLength);

		children = root.children();
		childLength = children.length;
		assertEquals("The root node should have 1 child.", 1, childLength);

		TreeNode<Object> theChild = children[0];
		assertEquals("The root's child should be child.", child, theChild);

		// Now remove the child from the root.
		child.removeFromParent();

		isRoot = child.isRoot();
		assertTrue("The child node should now be the root of its own subtree.",
				isRoot == true);

		depth = child.depth();
		assertEquals(
				"The child node should have depth of 0 since it is now the root.",
				0, depth);

		parent = child.getParent();
		assertEquals(
				"The child should not have a parent since it is now the root.",
				null, parent);

		hasChildren = root.hasChildren();
		assertTrue("The root node should no longer have any children.",
				hasChildren == false);
	}

	/**
	 * This tests a tree only 1 level deep, but there are several first level
	 * children. This is useful to test that siblings are kept correctly before
	 * testing multiple level trees.
	 */
	@Test
	public void testMultipleChildren() {
		TreeNode<Object> root = new TreeNode<Object>();
		TreeNode<Object> child1 = new TreeNode<Object>();
		TreeNode<Object> child2 = new TreeNode<Object>();
		TreeNode<Object> child3 = new TreeNode<Object>();

		// Add the children.
		root.add(child1, 0);
		root.add(child3, 1); // Add with index, but really appending child
		root.add(child2, 1); // Add out of order to test insertion in the middle
								// of the children

		TreeNode<Object>[] children = root.children();
		assertEquals("child1 should be the first child.", child1, children[0]);
		assertEquals("child2 should be the second child.", child2, children[1]);
		assertEquals("child3 should be the third child.", child3, children[2]);

		// Verify the properties of the children.
		for (int i = 0; i < children.length; i++) {
			TreeNode<Object> child = children[i];

			int index = child.index();
			assertEquals("child" + (i + 1) + " should have an index of " + i,
					i, index);

			boolean isRoot = child.isRoot();
			assertTrue("child" + (i + 1) + " should not be the root node.",
					isRoot == false);

			int depth = child.depth();
			assertEquals(
					"child"
							+ (i + 1)
							+ " should have depth of 1 since it is a first level node.",
					1, depth);

			TreeNode<Object> parent = child.getParent();
			assertEquals("child" + (i + 1)
					+ "'s parent should be the root node.", root, parent);

			boolean hasChildren = child.hasChildren();
			assertTrue(
					"child"
							+ (i + 1)
							+ " should not have any children because none have been added.",
					hasChildren == false);
		}

		// Remove the middle child.
		TreeNode<Object> removed = root.remove(1);
		assertEquals("The removed node should be child2.", child2, removed);
		assertEquals("child2 should now have a depth of 0.", 0, removed.depth());

		children = root.children();
		assertEquals("The root should now have 2 children.", 2, children.length);
		assertEquals("child3 should be the second child.", child3, children[1]);

		// Add the middle child back in.
		root.add(removed, 1);
		children = root.children();
		TreeNode<Object> node = children[1];

		assertEquals("The second node should be child2 again.", child2, node);
		assertEquals("child2's parent should be the root again.", root,
				child2.getParent());
		assertEquals("child2's depth should be 1 again.", 1, child2.depth());

		// Remove all the children.
		root.remove(1);
		root.remove(1);
		child1.removeFromParent();
		assertTrue("The root should not have any children now.",
				root.hasChildren() == false);
	}

	/**
	 * This tests that multiple levels of the tree work correctly. Each level
	 * has only 1 child. This is a simple test to verify depth works before
	 * moving onto more complex trees.
	 */
	@Test
	public void testMultipleLevels() {
		TreeNode<Object> root = new TreeNode<Object>();
		TreeNode<Object> depth1 = new TreeNode<Object>();
		TreeNode<Object> depth2 = new TreeNode<Object>();
		TreeNode<Object> depth3 = new TreeNode<Object>();

		// Add the children.
		root.add(depth1);
		depth2.add(depth3);
		depth1.add(depth2);

		// Verify the properties of the nodes.
		assertTrue("The root should have 1 child.", root.hasChildren() == true);
		assertEquals("The root should have a depth of 0.", 0, root.depth());
		assertEquals("The root should not have a parent.", null,
				root.getParent());

		assertTrue("depth1 should have 1 child.", depth1.hasChildren() == true);
		assertEquals("depth1 should have a depth of 1.", 1, depth1.depth());
		assertEquals("depth1 should have root as its parent.", root,
				depth1.getParent());

		assertTrue("depth2 should have 1 child.", depth2.hasChildren() == true);
		assertEquals("depth2 should have a depth of 2.", 2, depth2.depth());
		assertEquals("depth2 should have depth1 as its parent.", depth1,
				depth2.getParent());

		assertTrue("depth3 should have not have any children.",
				depth3.hasChildren() == false);
		assertEquals("depth3 should have a depth of 3.", 3, depth3.depth());
		assertEquals("depth3 should have depth2 as its parent.", depth2,
				depth3.getParent());

		// Cut the tree in 1/2.
		depth2.removeFromParent();
		assertTrue("depth2 should now be a root.", depth2.isRoot() == true);
		assertEquals("depth2 should now have a depth of 0.", 0, depth2.depth());
		assertEquals("depth3 should now have a depth of 1.", 1, depth3.depth());
		assertTrue("depth1 should not have any children now.",
				depth1.hasChildren() == false);
	}

	/**
	 * This tests a tree with multiple varying depths and multiple varying
	 * amounts of children at each depth.
	 */
	@Test
	public void testBigTree() {
		String testData = "";

		TreeNode<String> root = new TreeNode<String>();
		TreeNode<String> d1c0 = new TreeNode<String>(testData);
		TreeNode<String> d1c1 = new TreeNode<String>(testData);
		TreeNode<String> d1c2 = new TreeNode<String>(testData);
		TreeNode<String> d2c1c0 = new TreeNode<String>(testData);
		TreeNode<String> d2c1c1 = new TreeNode<String>(testData);
		TreeNode<String> d2c1c2 = new TreeNode<String>(testData);
		TreeNode<String> d2c2c0 = new TreeNode<String>(testData);
		TreeNode<String> d2c2c1 = new TreeNode<String>(testData);
		TreeNode<String> d3c2c0c0 = new TreeNode<String>(testData);
		TreeNode<String> d3c2c0c1 = new TreeNode<String>(testData);

		root.add(d1c0);
		root.add(d1c1);
		root.add(d1c2);

		d1c1.add(d2c1c1); // Second child
		d1c1.add(d2c1c0, 0); // First child
		d1c1.add(d2c1c2); // Third child

		d1c2.add(d2c2c0);
		d2c2c0.add(d3c2c0c0);
		d2c2c0.add(d3c2c0c1);

		d1c2.add(d2c2c1);

		// Verify the tree structure.
		assertEquals("root should be the parent of d1c0", root,
				d1c0.getParent());
		assertEquals("root should be the parent of d1c1", root,
				d1c1.getParent());
		assertEquals("root should be the parent of d1c2", root,
				d1c2.getParent());

		assertEquals("d1c1 should be the parent of d2c1c0", d1c1,
				d2c1c0.getParent());
		assertEquals("d1c1 should be the parent of d2c1c1", d1c1,
				d2c1c1.getParent());
		assertEquals("d1c1 should be the parent of d2c1c2", d1c1,
				d2c1c2.getParent());

		assertEquals("d1c2 should be the parent of d2c2c0", d1c2,
				d2c2c0.getParent());
		assertEquals("d1c2 should be the parent of d2c2c1", d1c2,
				d2c2c1.getParent());

		assertEquals("d2c2c0 should be the parent of d3c2c0c0", d2c2c0,
				d3c2c0c0.getParent());
		assertEquals("d2c2c0 should be the parent of d3c2c0c1", d2c2c0,
				d3c2c0c1.getParent());

		// Verify the depths of some of the nodes.
		assertEquals("root should have a depth of 0", 0, root.depth());
		assertEquals("d1c1 should have a depth of 1", 1, d1c1.depth());
		assertEquals("d2c2c0 should have a depth of 2", 2, d2c2c0.depth());
		assertEquals("d3c2c0c1 should have a depth of 3", 3, d3c2c0c1.depth());

		// Verify we can traverse the tree from root to d3c2c0c0.
		TreeNode<String> node = root.children()[2]; // d1c2
		node = node.children()[0]; // d2c2c0
		node = node.children()[0]; // d3c2c0c0
		assertEquals("We should have tranversed the tree to d3c2c0c0.",
				d3c2c0c0, node);

		assertEquals(
				"Should have test data as user object from node d3c2c0c0.",
				testData, node.getData());
	}

	@Test
	public void testSize() {
		assertEquals(17, getTree().size());
	}

	@Test
	public void testIteratorOneElement() {
		TreeNode<Integer> tree = new TreeNode<Integer>(42);
		boolean loopAccessed = false;
		for (int integer : tree) {
			assertEquals(42, integer);
			loopAccessed = true;
		}
		assertTrue(loopAccessed);
	}

	@Test
	public void testIteratorFull() {
		int i = 0;
		for (int value : getTree()) {
			assertEquals("", ++i, value);
		}
		assertEquals(getTree().size(), i);
	}

	@Test
	public void testIteratorPartial() {
		int[] order = new int[] { 3, 4, 5, 6, 7 };
		TreeNode<Integer> subTree = getTree().children()[1].children()[1];
		int i = 0;
		for (int value : subTree) {
			assertEquals("", order[i++], value);
		}
		assertEquals(order.length, i);
	}

	@Test
	public void testBfs() {
		int[] order = new int[] { 17, 1, 8, 16, 2, 7, 11, 12, 15, 5, 6, 9, 10,
				13, 14, 3, 4 };
		int i = 0;
		for (Iterator<Integer> iterator = getTree().bfs(); iterator.hasNext(); i++) {
			int next = (int) iterator.next();
			System.out.println(next);
			assertEquals(order[i], next);
		}
		assertEquals(order.length, i);
	}

	@Test
	public void findNode() {
		List<TreeNode<Integer>> treeNodes = getTree().find(5);
		assertEquals(1, treeNodes.size());
		TreeNode<Integer> treeNode = treeNodes.get(0);
		assertNotNull(treeNode);
		assertEquals(new Integer(5), treeNode.getData());
		assertEquals(2, treeNode.children().length);
		assertEquals(3, treeNode.children()[0]);
		assertEquals(4, treeNode.children()[1]);
		assertEquals(7, treeNode.getParent());
		assertEquals(8, treeNode.getParent().getParent());
		assertEquals(16, treeNode.getParent().getParent().getParent());
		assertNull(treeNode.getParent().getParent().getParent().getParent());
	}

	@Test
	public void findNodes() {
		TreeNode<Integer> tree = getTree();

		List<TreeNode<Integer>> fives = tree.find(5);
		assertEquals(1, fives.size());
		TreeNode<Integer> five = fives.get(0);
		assertNotNull(five);
		five.add(new TreeNode<Integer>(16));

		List<TreeNode<Integer>> sixteens = tree.find(16);
		assertEquals(2, sixteens.size());
		assertEquals(new Integer(16), sixteens.get(0).getData());
		assertEquals(new Integer(16), sixteens.get(1).getData());
		assertNull(sixteens.get(0).getParent());
		assertEquals(five, sixteens.get(1).getParent());
		assertEquals(0, sixteens.get(1).children().length);
	}

	@Test
	public void isAncestorDescendant() {
		TreeNode<Integer> tree = getTree();
		TreeNode<Integer> n17 = tree.find(17).get(0);
		assertNotNull(n17);
		TreeNode<Integer> n7 = tree.find(7).get(0);
		assertNotNull(n7);
		TreeNode<Integer> n2 = tree.find(2).get(0);
		assertNotNull(n2);
		TreeNode<Integer> n3 = tree.find(3).get(0);
		assertNotNull(n3);

		assertFalse(n17.isAncestorOf(n17));
		assertTrue(n17.isAncestorOf(n7));
		assertTrue(n17.isAncestorOf(n2));
		assertTrue(n17.isAncestorOf(n3));

		assertFalse(n7.isAncestorOf(n17));
		assertFalse(n7.isAncestorOf(n7));
		assertFalse(n7.isAncestorOf(n2));
		assertTrue(n7.isAncestorOf(n3));

		assertFalse(n2.isAncestorOf(n17));
		assertFalse(n2.isAncestorOf(n7));
		assertFalse(n2.isAncestorOf(n2));
		assertFalse(n2.isAncestorOf(n3));

		assertFalse(n3.isAncestorOf(n17));
		assertFalse(n3.isAncestorOf(n7));
		assertFalse(n3.isAncestorOf(n2));
		assertFalse(n3.isAncestorOf(n3));

		assertFalse(n17.isDescendantOf(n17));
		assertFalse(n17.isDescendantOf(n7));
		assertFalse(n17.isDescendantOf(n2));
		assertFalse(n17.isDescendantOf(n3));

		assertTrue(n7.isDescendantOf(n17));
		assertFalse(n7.isDescendantOf(n7));
		assertFalse(n7.isDescendantOf(n2));
		assertFalse(n7.isDescendantOf(n3));

		assertTrue(n2.isDescendantOf(n17));
		assertFalse(n2.isDescendantOf(n7));
		assertFalse(n2.isDescendantOf(n2));
		assertFalse(n2.isDescendantOf(n3));

		assertTrue(n3.isDescendantOf(n17));
		assertTrue(n3.isDescendantOf(n7));
		assertFalse(n3.isDescendantOf(n2));
		assertFalse(n3.isDescendantOf(n3));
	}
}