package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;

public class DistributionUtils {

	/**
	 * Marker interface for widths.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static interface Width {
		public static final double DEFAULT_MIN_WIDTH = 0.0;
	}

	/**
	 * Absolute widths.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class AbsoluteWidth implements Width {
		private final double width;

		/**
		 * Constructs a new {@link AbsoluteWidth} with the given width.
		 * 
		 * @param width
		 */
		public AbsoluteWidth(double width) {
			this.width = width;
		}

		/**
		 * Constructs a new {@link AbsoluteWidth} with the given width.
		 * 
		 * @param width
		 */
		public AbsoluteWidth(int width) {
			this(Integer.valueOf(width).doubleValue());
		}

		/**
		 * Returns the {@link AbsoluteWidth}‘s width.
		 * 
		 * @return
		 */
		public double getWidth() {
			return this.width;
		}
	}

	/**
	 * Relative widths with an optional absolute minimal width.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class RelativeWidth implements Width {
		private final double width;
		private final AbsoluteWidth minWidth;

		public RelativeWidth(double width, double minWidth) {
			this.width = width;
			this.minWidth = new AbsoluteWidth(minWidth);
		}

		public RelativeWidth(double width, int minWidth) {
			this(width, Integer.valueOf(minWidth).doubleValue());
		}

		public RelativeWidth(int width, double minWidth) {
			this(Integer.valueOf(width).doubleValue(), minWidth);
		}

		public RelativeWidth(int width, int minWidth) {
			this(Integer.valueOf(width).doubleValue(), Integer
					.valueOf(minWidth).doubleValue());
		}

		public RelativeWidth(double width) {
			this(width, DEFAULT_MIN_WIDTH);
		}

		public RelativeWidth(int width) {
			this(Integer.valueOf(width).doubleValue(), DEFAULT_MIN_WIDTH);
		}

		public double getWidth() {
			return this.width;
		}

		public AbsoluteWidth getMinWidth() {
			return this.minWidth;
		}
	}

	/**
	 * Given an array of {@link AbsoluteWidth}s and {@link RelativeWidth}s (e.g.
	 * 20 resp. 17%) and a target width returns the {@link AbsoluteWidth} for
	 * each given number to fill up the target width.
	 * 
	 * e.g. { 20, 50%, 50%} and a target width of 100 results in { 20, 40, 40 }.
	 * 
	 * The target width may be exceeded if the sum of absolute numbers equals or
	 * is greater 100. Negative distribution is replaced by 0.
	 * 
	 * @param rawDistribution
	 * @return
	 */
	public static double[] distribute(Width[] widths, int targetWidth) {
		double[] distribution = new double[widths.length];
		double fixedWidth = 0;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] instanceof AbsoluteWidth) {
				double width = ((AbsoluteWidth) widths[i]).getWidth();
				distribution[i] = width;
				fixedWidth += width;
			}
		}
		targetWidth -= fixedWidth;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] instanceof RelativeWidth) {
				RelativeWidth width = (RelativeWidth) widths[i];
				distribution[i] = Math.max(Math.round(targetWidth
						* width.getWidth()), width.getMinWidth().getWidth());
			}
		}
		return distribution;
	}

	/**
	 * Given an array of relative and absolute numbers (e.g. 20 resp. 17%) and a
	 * target width returns the absolute widths for each given number to fill up
	 * the target width.
	 * 
	 * e.g. { 20, 50%, 50%} and a target width of 100 results in { 20, 40, 40 }.
	 * 
	 * The target width may be exceeded if the sum of absolute numbers equals or
	 * is greater 100. Negative distribution is replaced by 1.
	 * 
	 * @param rawDistribution
	 * @return
	 */
	public static List<Double> distribute(List<Width> widths, int targetWidth) {
		double[] dist = distribute(widths.toArray(new Width[widths.size()]),
				targetWidth);
		List<Double> distribution = new ArrayList<Double>(dist.length);
		for (double x : dist) {
			distribution.add(x);
		}
		return distribution;
	}

}
