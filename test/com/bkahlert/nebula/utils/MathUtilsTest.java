package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class MathUtilsTest {
	@Test
	public void testCalcRadianBetweenPoints() {
		assertEquals(-45d, Math.toDegrees(MathUtils.calcRadianBetweenPoints(
				new Point(1, 0), new Point(0, 1))), 0.0000001d);

		assertEquals(45d, Math.toDegrees(MathUtils.calcRadianBetweenPoints(
				new Point(0, 0), new Point(1, 1))), 0.0000001d);

		assertEquals(90d, Math.toDegrees(MathUtils.calcRadianBetweenPoints(
				new Point(0, 1), new Point(0, 0))), 0.0000001d);
	}

	@Test
	public void testResizeRectangle() {
		assertEquals(new Rectangle(0, 0, 0, 0),
				MathUtils.resizeRectangle(new Point(0, 0), new Point(0, 0)));

		assertEquals(new Rectangle(0, 0, 10, 10),
				MathUtils.resizeRectangle(new Point(1, 1), new Point(10, 10)));

		assertEquals(new Rectangle(0, 5, 10, 10),
				MathUtils.resizeRectangle(new Point(1, 1), new Point(10, 20)));

		assertEquals(new Rectangle(6, 6, 0, 0), MathUtils.resizeRectangle(
				new Point(0, 0), new Rectangle(6, 6, 0, 0)));

		assertEquals(new Rectangle(6, 6, 10, 10), MathUtils.resizeRectangle(
				new Point(1, 1), new Rectangle(6, 6, 10, 10)));

		assertEquals(new Rectangle(6, 11, 10, 10), MathUtils.resizeRectangle(
				new Point(1, 1), new Rectangle(6, 6, 10, 20)));
	}

	@Test
	public void testCalcScaleFactor() {
		assertTrue(Double.isNaN(MathUtils.calcScaleFactor(new Point(0, 0),
				new Point(0, 0))));

		assertEquals(10,
				MathUtils.calcScaleFactor(new Point(1, 1), new Point(10, 10)),
				0.0000001d);

		assertEquals(10,
				MathUtils.calcScaleFactor(new Point(1, 1), new Point(10, 20)),
				0.0000001d);

		assertTrue(Double.isNaN(MathUtils.calcScaleFactor(new Point(0, 0),
				new Rectangle(6, 6, 0, 0))));

		assertEquals(10, MathUtils.calcScaleFactor(new Point(1, 1),
				new Rectangle(6, 6, 10, 10)), 0.0000001d);

		assertEquals(10, MathUtils.calcScaleFactor(new Point(1, 1),
				new Rectangle(6, 6, 10, 20)), 0.0000001d);
	}

	@Test(expected = AssertionFailedException.class)
	public void testMinNull() {
		MathUtils.min(null);

	}

	@Test(expected = AssertionFailedException.class)
	public void testMinEmpty() {
		MathUtils.min();
	}

	@Test
	public void testMin() {
		assertEquals(1, MathUtils.min(1, 2, 3));
		assertEquals(1, MathUtils.min(3, 2, 1));
		assertEquals(1, MathUtils.min(3, 1, 2));

		assertEquals(1, MathUtils.min(100, 1, 1));
		assertEquals(-1, MathUtils.min(100, Integer.MAX_VALUE, -1));
	}
}
