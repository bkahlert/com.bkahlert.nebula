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
		public static final double DEFAULT_MIN_WIDTH = 1.0;
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
	 * Negative distribution is replaced by 0.
	 * 
	 * @param rawDistribution
	 * @return
	 */
	public static double[] distribute(Width[] widths, final int targetWidth) {
		boolean allowOverflow = false; // TODO make parameter
		boolean roundTargetWidth = true; // TODO make parameter (if true, sum of
											// rounded result is targetWidth)

		double[] distribution = new double[widths.length];
		if (widths.length == 0) {
			return distribution;
		}

		double widthAvailable = targetWidth;

		double fixedWidth = 0;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] instanceof AbsoluteWidth) {
				double width = Math.max(((AbsoluteWidth) widths[i]).getWidth(),
						Width.DEFAULT_MIN_WIDTH);
				distribution[i] = width;
				fixedWidth += width;
			}
		}
		widthAvailable -= fixedWidth;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] instanceof RelativeWidth) {
				RelativeWidth width = (RelativeWidth) widths[i];
				distribution[i] = Math.max(Math.round(widthAvailable
						* width.getWidth()), width.getMinWidth().getWidth());
			}
		}

		if (!allowOverflow) {
			// increase or decrease proportionally till targetWidth is met
			double sum = 0;
			for (int i = 0; i < distribution.length; i++) {
				sum += distribution[i];
			}
			double difference = targetWidth - sum;
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] += (distribution[i] / sum) * difference;
			}

			if (roundTargetWidth) {
				// increase or decrease to targetWidth is met
				int maxIndex = 0;
				double maxWidth = -1;
				sum = 0;
				for (int i = 0; i < distribution.length; i++) {
					sum += Math.round(distribution[i]);
					if (distribution[i] > maxWidth) {
						maxWidth = distribution[i];
						maxIndex = i;
					}
				}
				distribution[maxIndex] += targetWidth - sum;
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
