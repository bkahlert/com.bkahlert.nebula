package com.bkahlert.nebula.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.bkahlert.nebula.utils.Pair;

/**
 * <code>getData</code> objects can be combined into a tree. The tree is not a
 * special tree such as a balanced tree. It can have any number of levels and
 * each node can have any number of children.
 * <p>
 * Each tree node may have at most one parent and 0 or more children.
 * <code>getData</code> provides operations for examining and modifying a node's
 * parent and children. A node's tree is the set of all nodes that can be
 * reached by starting at the node and following all the possible links to
 * parents and children. A node with no parent is the root of its tree; a node
 * with no children is a leaf. A tree may consist of many subtrees, each node
 * acting as the root for its own subtree.
 * <p>
 * Every <code>getData</code> can hold a reference to one data object. It is up
 * to the developer to decide what the object is and how to use it. For example
 * in maintaining the golf course tree the data object is an
 * <code>ICourseElement</code>.
 * <p>
 * <i>This is not a thread safe class.</i> If used from multiple threads it must
 * be manually sychronized by your code.
 *
 * @param <T>
 */
public class TreeNode<T> implements Iterable<T> {
	/**
	 * This node's parent node. If this is the root of the tree then the parent
	 * will be <code>null</code>.
	 */
	private TreeNode<T> parent;

	/**
	 * An array of all this node's child nodes. The array will always exist
	 * (i.e. never <code>null</code>) and be of length zero if this is a leaf
	 * node.
	 * <p>
	 * This is an array instead of a <code>Vector</code> to favor speed of
	 * accessing the children. The array takes longer on adds because of array
	 * copying and management. However to get the array it can just be returned
	 * instead of creating a new array from the <code>Vector</code> each time.
	 * The tree is used frequently enough for reading course elements that this
	 * difference makes a small impact on rendering.
	 */
	@SuppressWarnings("unchecked")
	private TreeNode<T>[] children = new TreeNode[0];

	/**
	 * Constructs a tree node object. It can become the root of a tree. Or it
	 * can become a child of another node by calling the other node's
	 * <code>add</code> method.
	 * <p>
	 * There is no data object attached to the node. Call
	 * <code>{@link #setData(Object)}</code> to attach one.
	 */
	public TreeNode() {
		// Nothing needed.
	}

	/**
	 * Constructs a copy of a tree node object.
	 */
	public TreeNode(TreeNode<T> treeNode) {
		this.parent = treeNode.parent;
		this.children = treeNode.children;
		this.data = treeNode.data;
	}

	/**
	 * Constructs a tree node object. It can become the root of a tree. Or it
	 * can become a child of another node by calling the other node's
	 * <code>add</code> method.
	 *
	 * @param data
	 *            is an object this node encapsulates. It is up to the developer
	 *            to maintain its type. To get the object back out call
	 *            <code>{@link #getData(Object)}</code>.
	 */
	public TreeNode(T data) {
		this.data = data;
	}

	/**
	 * Constructs a tree node object. It can become the root of a tree. Or it
	 * can become a child of another node by calling the other node's
	 * <code>add</code> method.
	 *
	 * @param data
	 *            is an object this node encapsulates. It is up to the developer
	 *            to maintain its type. To get the object back out call
	 *            <code>{@link #TreeNode()}</code>.
	 */
	public TreeNode(T data, List<TreeNode<T>> nodes) {
		this.data = data;
		if (nodes != null) {
			for (TreeNode<T> node : nodes) {
				this.add(node);
			}
		}
	}

	/**
	 * Adds the <code>child</code> node to this container making this its
	 * parent.
	 *
	 * @param child
	 *            is the node to add to the tree as a child of <code>this</code>
	 *
	 * @param index
	 *            is the position within the children list to add the child. It
	 *            must be between 0 (the first child) and the total number of
	 *            current children (the last child). If it is negative the child
	 *            will become the last child.
	 */
	public void add(TreeNode<T> child, int index) {
		// Add the child to the list of children.
		if (index < 0 || index == this.children.length) // then append
		{
			@SuppressWarnings("unchecked")
			TreeNode<T>[] newChildren = new TreeNode[this.children.length + 1];
			System.arraycopy(this.children, 0, newChildren, 0,
					this.children.length);
			newChildren[this.children.length] = child;
			this.children = newChildren;
		} else if (index > this.children.length) {
			throw new IllegalArgumentException("Cannot add child to index "
					+ index + ".  There are only " + this.children.length
					+ " children.");
		} else // insert
		{
			@SuppressWarnings("unchecked")
			TreeNode<T>[] newChildren = new TreeNode[this.children.length + 1];
			if (index > 0) {
				System.arraycopy(this.children, 0, newChildren, 0, index);
			}
			newChildren[index] = child;
			System.arraycopy(this.children, index, newChildren, index + 1,
					this.children.length - index);
			this.children = newChildren;
		}

		// Set the parent of the child.
		child.parent = this;
	}

	/**
	 * Adds the <code>child</code> node to this container making this its
	 * parent. The child is appended to the list of children as the last child.
	 */
	public void add(TreeNode<T> child) {
		this.add(child, -1);
	}

	/**
	 * Removes the child at position <code>index</code> from the tree.
	 *
	 * @param index
	 *            is the position of the child. It should be between 0 (the
	 *            first child) and the total number of children minus 1 (the
	 *            last child).
	 * @return The removed child node. This will be <code>null</code> if no
	 *         child exists at the specified <code>index</code>.
	 */
	public TreeNode<T> remove(int index) {
		if (index < 0 || index >= this.children.length) {
			throw new IllegalArgumentException(
					"Cannot remove element with index " + index
							+ " when there are " + this.children.length
							+ " elements.");
		}

		// Get a handle to the node being removed.
		TreeNode<T> node = this.children[index];
		node.parent = null;

		// Remove the child from this node.
		@SuppressWarnings("unchecked")
		TreeNode<T>[] newChildren = new TreeNode[this.children.length - 1];
		if (index > 0) {
			System.arraycopy(this.children, 0, newChildren, 0, index);
		}
		if (index != this.children.length - 1) {
			System.arraycopy(this.children, index + 1, newChildren, index,
					this.children.length - index - 1);
		}
		this.children = newChildren;

		return node;
	}

	/**
	 * Removes this node from its parent. This node becomes the root of a
	 * subtree where all of its children become first level nodes.
	 * <p>
	 * Calling this on the root node has no effect.
	 */
	public void removeFromParent() {
		if (this.parent != null) {
			int position = this.index();
			this.parent.remove(position);
			this.parent = null;
		}
	}

	/**
	 * Gets the parent node of this one.
	 *
	 * @return The parent of this node. This will return <code>null</code> if
	 *         this node is the root node in the tree.
	 */
	public TreeNode<T> getParent() {
		return this.parent;
	}

	/**
	 * Returns if this node is the root node in the tree or not.
	 *
	 * @return <code>true</code> if this node is the root of the tree;
	 *         <code>false</code> if it has a parent.
	 */
	public boolean isRoot() {
		if (this.parent == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets a list of all the child nodes of this node.
	 *
	 * @return An array of all the child nodes. The array will be the size of
	 *         the number of children. A leaf node will return an empty array,
	 *         not <code>null</code>.
	 */
	public TreeNode<T>[] children() {
		return this.children;
	}

	/**
	 * Returns if this node has children or if it is a leaf node.
	 *
	 * @return <code>true</code> if this node has children; <code>false</code>
	 *         if it does not have any children.
	 */
	public boolean hasChildren() {
		if (this.children.length == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Gets the position of this node in the list of siblings managed by the
	 * parent node. This node can be obtained by
	 * <code>this = parent.children[this.index()]</code>.
	 *
	 * @return The index of the child array of this node's parent. If this is
	 *         the root node it will return -1.
	 */
	public int index() {
		if (this.parent != null) {
			for (int i = 0;; i++) {
				Object node = this.parent.children[i];

				if (this == node) {
					return i;
				}
			}
		}

		// Only ever make it here if this is the root node.
		return -1;
	}

	/**
	 * Gets this node's depth in the tree. The root node will have a depth of 0,
	 * first-level nodes will have a depth of 1, and so on.
	 *
	 * @return The depth of this node in the tree.
	 */
	public int depth() {
		int depth = this.recurseDepth(this.parent, 0);
		return depth;
	}

	/**
	 * Recursive helper method to calculate the depth of a node. The caller
	 * should pass its parent and an initial depth of 0.
	 * <p>
	 * A recursive approach is used so that when a node that is part of a tree
	 * is removed from that tree, we do not need to recalculate the depth of
	 * every node in that subtree.
	 *
	 * @param node
	 *            is the node to recursively check for its depth. This should be
	 *            set to <code>parent</code> by the caller.
	 * @param depth
	 *            is the depth of the current node (i.e. the child of
	 *            <code>node</code>). This should be set to 0 by the caller.
	 */
	private int recurseDepth(TreeNode<T> node, int depth) {
		if (node == null) // reached top of tree
		{
			return depth;
		} else {
			return this.recurseDepth(node.parent, depth + 1);
		}
	}

	/**
	 * A handle to the programmer assigned object encapsulated by this node.
	 * This will be <code>null</code> when the user has not assigned any data to
	 * this node.
	 */
	private T data;

	/**
	 * Attaches a user defined object to this node. Only one object can be
	 * attached to a node.
	 *
	 * @param data
	 *            is the programmer defined object to attach to this node in the
	 *            tree. Set it to <code>null</code> to clear any objects.
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * Gets the user defined object attached to this node. It must be cast back
	 * to what it was inserted as. It is up to the developer to make this cast.
	 *
	 * @return The programmer defined object attached to this node in the tree.
	 *         Returns <code>null</code> if no object is attached.
	 */
	public T getData() {
		return this.data;
	}

	public Iterator<T> bfs() {
		return new Iterator<T>() {

			private final List<TreeNode<T>> queue = new ArrayList<TreeNode<T>>();

			{
				this.queue.add(TreeNode.this);
			}

			@Override
			public boolean hasNext() {
				return this.queue.size() > 0;
			}

			@Override
			public T next() {
				TreeNode<T> current = this.queue.remove(0);
				for (TreeNode<T> child : current.children) {
					this.queue.add(child);
				}
				T next = current.getData();
				return next;
			}

			@Override
			public void remove() {
				return;
			}
		};
	}

	public Iterator<T> postorder() {
		return new Iterator<T>() {

			private final TreeNode<T> stop = TreeNode.this;
			private TreeNode<T> current;

			{
				TreeNode<T> firstChild = this.stop;
				while (firstChild.hasChildren()) {
					firstChild = firstChild.children[0];
				}
				this.current = firstChild;
			}

			@Override
			public boolean hasNext() {
				return this.current != null;
			}

			@Override
			public T next() {
				T next = this.current.getData();
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}
				if (this.current.parent == null || this.current == this.stop) {
					this.current = null;
				} else if (this.current.parent.children.length > this.current
						.index() + 1) {
					TreeNode<T> sisterNode = this.current.parent.children[this.current
							.index() + 1];
					TreeNode<T> firstChild = sisterNode;
					while (firstChild.hasChildren()) {
						firstChild = firstChild.children[0];
					}
					this.current = firstChild;
				} else {
					this.current = this.current.parent;
				}
				return next;
			}

			@Override
			public void remove() {
				return;
			}
		};
	}

	@Override
	public Iterator<T> iterator() {
		return this.postorder();
	}

	/**
	 * Finds a given value in the {@link TreeNode} and its children.
	 * <p>
	 * If the root matches it is also contained in the result.
	 *
	 * @param value
	 * @return
	 */
	public List<TreeNode<T>> find(T value) {
		List<TreeNode<T>> foundNodes = new ArrayList<TreeNode<T>>();
		if (this.getData().equals(value)) {
			foundNodes.add(this);
		}
		for (TreeNode<T> treeNode : this.children()) {
			foundNodes.addAll(treeNode.find(value));
		}
		return foundNodes;
	}

	public int size() {
		int size = 0;
		for (@SuppressWarnings("unused")
		T value : this) {
			size++;
		}
		return size;
	}

	public boolean isAncestorOf(TreeNode<T> node) {
		if (node == null) {
			return false;
		}
		if (node.parent == this) {
			return true;
		}
		return this.isAncestorOf(node.parent);
	}

	public boolean isDescendantOf(TreeNode<T> node) {
		return node.isAncestorOf(this);
	}

	/**
	 * Returns all parent - child relations.
	 *
	 * @return each element consists of the parent (first element) and child
	 *         (second element)
	 */
	public List<Pair<T, T>> getParentRelations() {
		List<Pair<T, T>> relations = new ArrayList<>();
		this.getParentRelations(relations);
		return relations;
	}

	private List<Pair<T, T>> getParentRelations(List<Pair<T, T>> relations) {
		relations.add(new Pair<>(this.parent != null ? this.parent.data : null,
				this.data));
		for (TreeNode<T> child : this.children) {
			child.getParentRelations(relations);
		}
		return relations;
	}

	public List<Pair<T, T>> getAncestorRelations() {
		Map<TreeNode<T>, Set<TreeNode<T>>> ancestors = new HashMap<>();
		this.getAncestorRelations(ancestors);

		List<Pair<T, T>> ancestors_ = new ArrayList<>();
		for (Entry<TreeNode<T>, Set<TreeNode<T>>> entry : ancestors.entrySet()) {
			T child = entry.getKey().data;
			for (TreeNode<T> ancestor : entry.getValue()) {
				ancestors_.add(new Pair<>(ancestor.data, child));
			}
		}
		return ancestors_;
	}

	private void getAncestorRelations(
			Map<TreeNode<T>, Set<TreeNode<T>>> ancestors) {
		if (ancestors.containsKey(this)) {
			throw new RuntimeException("Implementation error");
		}

		HashSet<TreeNode<T>> currAncestors = new LinkedHashSet<>();
		if (this.parent != null) {
			currAncestors.add(this.parent);
			currAncestors.addAll(ancestors.get(this.parent));
		}
		ancestors.put(this, currAncestors);

		for (TreeNode<T> child : this.children) {
			child.getAncestorRelations(ancestors);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.children);
		result = prime * result
				+ ((this.data == null) ? 0 : this.data.hashCode());
		result = prime
				* result
				+ ((this.parent == null) ? 0 : System
						.identityHashCode(this.parent));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		TreeNode other = (TreeNode) obj;
		if (!Arrays.equals(this.children, other.children)) {
			return false;
		}
		if (this.data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!this.data.equals(other.data)) {
			return false;
		}
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		return true;
	}

}