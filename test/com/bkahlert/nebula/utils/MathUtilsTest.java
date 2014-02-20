package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

import com.bkahlert.nebula.utils.MathUtils;

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
}
