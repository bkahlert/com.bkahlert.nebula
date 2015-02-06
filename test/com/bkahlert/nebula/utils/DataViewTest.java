package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class DataViewTest {

	private static class DoubleLengthView extends DataView {

		private AtomicReference<Integer> refreshCounter;
		private MyList<? extends Object> list;

		public DoubleLengthView(MyList<? extends Object> list,
				AtomicReference<Integer> refreshCounter) {
			super(list);
			this.list = list;
			this.refreshCounter = refreshCounter;
		}

		public DoubleLengthView(DoubleLengthView view,
				AtomicReference<Integer> refreshCounter) {
			super(view);
			this.list = new MyList<>();
			this.list.add(null);
			this.refreshCounter = refreshCounter;
		}

		private int doubleLength;

		@Override
		protected void refresh() {
			this.refreshCounter.set(this.refreshCounter.get() + 1);
			this.doubleLength = this.list.size() * 2;
		}

		public int getDoubleLength() {
			this.checkAndRefresh();
			return this.doubleLength;
		}
	}

	private static class MyList<T> extends LinkedList<T> implements IDirtiable {

		private static final long serialVersionUID = 1L;
		private long lastModification;

		@Override
		public boolean add(T e) {
			this.modified();
			return super.add(e);
		}

		@Override
		public void modified() {
			this.lastModification = System.nanoTime();
		}

		@Override
		public long getLastModification() {
			return this.lastModification;
		}

	}

	@Test
	public void test() {

		MyList<String> strings = new MyList<>();
		strings.add("Hello");
		strings.add("World");

		AtomicReference<Integer> refreshCounter1 = new AtomicReference<>(0);
		DoubleLengthView view1 = new DoubleLengthView(strings, refreshCounter1);
		assertEquals(0, refreshCounter1.get().intValue());

		assertEquals(4, view1.getDoubleLength());
		assertEquals(1, refreshCounter1.get().intValue());

		assertEquals(4, view1.getDoubleLength());
		assertEquals(1, refreshCounter1.get().intValue());

		strings.add("!");

		assertEquals(6, view1.getDoubleLength());
		assertEquals(2, refreshCounter1.get().intValue());

		assertEquals(6, view1.getDoubleLength());
		assertEquals(2, refreshCounter1.get().intValue());

		// check dataview as a dependency
		AtomicReference<Integer> refreshCounter2 = new AtomicReference<>(0);
		DoubleLengthView view2 = new DoubleLengthView(view1, refreshCounter2);
		assertEquals(2, refreshCounter1.get().intValue());
		assertEquals(0, refreshCounter2.get().intValue());

		assertEquals(6, view1.getDoubleLength());
		assertEquals(2, refreshCounter1.get().intValue());
		assertEquals(2, view2.getDoubleLength());
		assertEquals(1, refreshCounter2.get().intValue());

		strings.remove(1);

		assertEquals(6, view1.getDoubleLength());
		assertEquals(2, refreshCounter1.get().intValue());
		assertEquals(2, view2.getDoubleLength());
		assertEquals(1, refreshCounter2.get().intValue());

		// we didn't override remove
		strings.modified();

		assertEquals(4, view1.getDoubleLength());
		assertEquals(3, refreshCounter1.get().intValue());
		assertEquals(2, view2.getDoubleLength());
		assertEquals(2, refreshCounter2.get().intValue());
	}

}
