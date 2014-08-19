package com.bkahlert.nebula.utils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

public class IteratorUtils {
	private static <T> T getUnvisitedChildNode(T n,
			IConverter<T, T[]> getChildren, Map<T, Boolean> visited) {
		T[] children = getChildren.convert(n);
		if (children != null) {
			for (T child : children) {
				if (!visited.containsKey(child) || !visited.get(child)) {
					return child;
				}
			}
		}
		return null;
	}

	public static class BreadthFirstIterator<T> implements Iterator<T> {
		private IConverter<T, T[]> getChildren;
		private Map<T, Boolean> visited;
		private final Queue<T> q = new LinkedList<T>();

		public BreadthFirstIterator(T root, IConverter<T, T[]> getChildren,
				boolean identity) {
			this.getChildren = getChildren;
			this.visited = identity ? new IdentityHashMap<T, Boolean>()
					: new HashMap<T, Boolean>();
			this.q.add(root);
			this.visited.put(root, true);
		}

		public BreadthFirstIterator(T root, IConverter<T, T[]> getChildren) {
			this(root, getChildren, false);
		}

		@Override
		public boolean hasNext() {
			return !this.q.isEmpty();
		}

		@Override
		public T next() {
			if (this.q.isEmpty()) {
				throw new NoSuchElementException();
			}
			T n = this.q.remove();
			T child = null;
			while ((child = getUnvisitedChildNode(n, this.getChildren,
					this.visited)) != null) {
				this.visited.put(child, true);
				this.q.add(child);
			}
			return n;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static class DepthFirstIterator<T> implements Iterator<T> {
		private IConverter<T, T[]> getChildren;
		private Map<T, Boolean> visited;
		private final Stack<T> s = new Stack<T>();

		private T next;

		public DepthFirstIterator(T root, IConverter<T, T[]> getChildren,
				boolean identity) {
			this.getChildren = getChildren;
			this.visited = identity ? new IdentityHashMap<T, Boolean>()
					: new HashMap<T, Boolean>();
			this.s.push(root);
			this.visited.put(root, true);
			this.next = root;
		}

		public DepthFirstIterator(T root, IConverter<T, T[]> getChildren) {
			this(root, getChildren, false);
		}

		@Override
		public boolean hasNext() {
			return this.next != null;
		}

		@Override
		public T next() {
			if (this.next == null) {
				throw new NoSuchElementException();
			}
			T rt = this.next;
			if (!this.s.isEmpty()) {
				T child = getUnvisitedChildNode(this.s.peek(),
						this.getChildren, this.visited);
				if (child != null) {
					this.visited.put(child, true);
					this.s.push(child);
					this.next = child;
				} else {
					this.s.pop();
				}
				if (child == null) {
					this.next();
				}
			} else {
				this.next = null;
			}
			return rt;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static <T> Iterable<T> bfs(final T root,
			final IConverter<T, T[]> getChildren) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new BreadthFirstIterator<T>(root, getChildren);
			}
		};
	}

	public static <T> Iterable<T> dfs(final T root,
			final IConverter<T, T[]> getChildren) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new DepthFirstIterator<T>(root, getChildren);
			}
		};
	}
}
