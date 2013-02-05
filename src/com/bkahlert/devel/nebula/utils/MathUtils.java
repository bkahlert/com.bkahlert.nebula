package com.bkahlert.devel.nebula.utils;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class MathUtils {
	/**
	 * Given a directed graph defined by two given point and the x-axis the
	 * method calculated the radian between those two graphs.
	 * <p>
	 * e.g. if you have a directed graph P1(0,0) and P2(1,1) the angle would be
	 * about 0.785rad (45°).
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double calcRadianBetweenPoints(Point p1, Point p2) {
		return Math.atan(((double) p1.y - (double) p2.y)
				/ ((double) p1.x - (double) p2.x));
	}

	/**
	 * Given a directed graph defined by two given point and the x-axis the
	 * method calculated the degree between those two graphs.
	 * <p>
	 * e.g. if you have a directed graph P1(0,0) and P2(1,1) the angle would be
	 * 45� (about 0.785rad).
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double calcDegreeBetweenPoints(Point p1, Point p2) {
		return Math.toDegrees(calcRadianBetweenPoints(p1, p2));
	}

	/**
	 * Calculate the maximum size a given rectangle (specified by its width and
	 * height) can have if it must be resized to given limits.
	 * 
	 * @param rectangle
	 * @param limits
	 * @return
	 */
	public static Rectangle resizeRectangle(Point rectangle, Point limits) {
		double r_old = (double) rectangle.x / (double) rectangle.y;
		double r_new = (double) limits.x / (double) limits.y;

		double x, y, w, h;
		if (r_old > r_new) {
			w = limits.x;
			h = w / r_old;
			x = 0;
			y = (limits.y - h) / 2;
		} else {
			h = limits.y;
			w = h * r_old;
			y = 0;
			x = (limits.x - w) / 2;
		}

		return new Rectangle((int) Math.round(x), (int) Math.round(y),
				(int) Math.round(w), (int) Math.round(h));
	}

	/**
	 * Calculate the maximum size a given rectangle (specified by its width and
	 * height) can have if it must be resized to given limits.
	 * 
	 * @param rectangle
	 * @param limits
	 * @return
	 */
	public static Rectangle resizeRectangle(Point rectangle, Rectangle limits) {
		Rectangle resizedRectangle = resizeRectangle(rectangle, new Point(
				limits.width, limits.height));
		resizedRectangle.x += limits.x;
		resizedRectangle.y += limits.y;
		return resizedRectangle;
	}

	/**
	 * Calculates by which factor a given rectangle may be scaled without
	 * exceeding the given limits.
	 * 
	 * @param rectangle
	 * @param limits
	 * @return
	 */
	public static double calcScaleFactor(Point rectangle, Point limits) {
		Rectangle resizedRectangle = resizeRectangle(rectangle, limits);
		return (double) resizedRectangle.width / (double) rectangle.x;
	}

	/**
	 * Calculates by which factor a given rectangle may be scaled without
	 * exceeding the given limits.
	 * 
	 * @param rectangle
	 * @param limits
	 * @return
	 */
	public static double calcScaleFactor(Point rectangle, Rectangle limits) {
		Rectangle resizedRectangle = resizeRectangle(rectangle, limits);
		return (double) resizedRectangle.width / (double) rectangle.x;
	}
}
