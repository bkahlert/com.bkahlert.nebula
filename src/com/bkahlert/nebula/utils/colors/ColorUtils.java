package com.bkahlert.nebula.utils.colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.MathUtils;

public class ColorUtils {

	public static double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

	private ColorUtils() {
		// no instantiation allowed
	}

	/**
	 * Increases a color's lightness
	 *
	 * @param rgb
	 *            that defines the color
	 * @param lightness
	 *            by which the color's lightness should be increased;<br/>
	 *            range: -1 (results in black) to +1 (+1 results in white)
	 * @return
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HLS and HSV</a>
	 */
	public static RGB addLightness(RGB rgb, float lightness) {
		if (lightness < -1 || lightness > 1) {
			throw new IndexOutOfBoundsException();
		}

		/*
		 * Convert to HLS; HLS and HLS are synonym
		 */
		HLS hls = ColorSpaceConverter.RGBtoHLS(rgb);

		/*
		 * Scale the lightness
		 */
		double newLightness = hls.getLightness() + lightness;
		if (newLightness < 0) {
			newLightness = 0;
		}
		if (newLightness > 1) {
			newLightness = 1;
		}
		hls.setLightness(newLightness);

		/*
		 * Convert back from HLS to RGB
		 */
		return ColorSpaceConverter.HLStoRGB(hls);
	}

	/**
	 * Increases a color's lightness
	 *
	 * @param rgb
	 *            that defines the color
	 * @param lightness
	 *            by which the color's lightness should be increased;<br/>
	 *            range: -1 (results in black) to +1 (+1 results in white)
	 * @return
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HLS and HSV</a>
	 */
	public static org.eclipse.swt.graphics.RGB addLightness(
			org.eclipse.swt.graphics.RGB rgb, float lightness) {
		return addLightness(new RGB(rgb), lightness).toClassicRGB();
	}

	/**
	 * Increases a color's lightness
	 *
	 * @param color
	 * @param lightness
	 *            by which the color's lightness should be increased;<br/>
	 *            range: -1 (results in black) to +1 (+1 results in white)
	 * @return newly created color. Must be disposed after usage!
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HLS and HSV</a>
	 */
	public static Color addLightness(Color color, float lightness) {
		return new Color(Display.getDefault(), addLightness(color.getRGB(),
				lightness));
	}

	/**
	 * Scales a color's lightness; the higher the ratio the lighter the color
	 *
	 * @param rgb
	 *            that defines the color
	 * @param scale
	 *            non-negative whereas 0 results in zero lightness = black color
	 * @return the scaled RGB
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/HSL_and_HSV">HLS and HSV</a>
	 */
	public static RGB scaleLightnessBy(RGB rgb, float scale) {
		if (scale < 0) {
			throw new IllegalArgumentException();
		}

		/*
		 * Convert to HLS; HLS and HLS are synonym
		 */
		HLS hls = ColorSpaceConverter.RGBtoHLS(rgb);

		/*
		 * Scale the lightness
		 */
		double newLightness = hls.getLightness() * scale;
		hls.setLightness(Math.min(newLightness, 1.0));

		/*
		 * Convert back from HLS to RGB
		 */
		return ColorSpaceConverter.HLStoRGB(hls);
	}

	public static RGB scaleSaturationBy(RGB rgb, float scale) {
		if (scale < 0) {
			throw new IllegalArgumentException();
		}

		/*
		 * Convert to HLS; HLS and HLS are synonym
		 */
		HLS hls = ColorSpaceConverter.RGBtoHLS(rgb);

		/*
		 * Scale the lightness
		 */
		double newSaturation = hls.getSaturation() * scale;
		hls.setSaturation(Math.min(newSaturation, 1.0));

		/*
		 * Convert back from HLS to RGB
		 */
		return ColorSpaceConverter.HLStoRGB(hls);
	}

	/**
	 * Generates a nice color according to <a href=
	 * "http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/"
	 * >martin.ankerl.com/2009/12/09/how-to-create-random-colors-
	 * programmatically</a>
	 *
	 * @return
	 */
	public static HLS getRandomHLS() {
		double hue = Math.random();
		hue += GOLDEN_RATIO_CONJUGATE;
		hue %= 1;
		return new HLS(hue, 0.5, 0.55);
	}

	/**
	 * Generates a nice color according to <a href=
	 * "http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/"
	 * >martin.ankerl.com/2009/12/09/how-to-create-random-colors-
	 * programmatically</a>
	 *
	 * @return
	 */
	public static RGB getRandomRGB() {
		return ColorSpaceConverter.HLStoRGB(getRandomHLS());
	}

	/**
	 * Returns a {@link Set} of evenly distributed {@link RGB}s creating a
	 * rainbow.
	 *
	 * @param num
	 * @return
	 */
	public static List<HLS> rainbow(int num) {
		return rainbow(num, new HLS(0.0, 0.5, 0.55), new HLS(1.0, 0.5, 0.55));
	}

	/**
	 * Returns a {@link Set} of evenly distributed {@link RGB}s creating a
	 * partical rainbow.
	 *
	 * @param num
	 * @param from
	 * @param to
	 * @return
	 */
	public static List<HLS> rainbow(int num, HLS from, HLS to) {
		return rainbow(num, from, to, 0);
	}

	/**
	 * Returns a {@link Set} of evenly distributed {@link RGB}s creating a
	 * partial rainbow with a given gap between the start and end {@link RGB}.
	 *
	 * @param num
	 * @param from
	 * @param to
	 * @param gap
	 *            num empty slows to the left and right of the rainbow
	 * @return
	 */
	public static List<HLS> rainbow(int num, HLS from, HLS to, int gap) {
		Assert.isLegal(num >= 0);
		List<HLS> rainbow = new ArrayList<>();
		double span = MathUtils.angDiff(from.getHue(), to.getHue());
		if (span == .0) {
			span = 1.0;
		}
		double diff = span / (num + 2 * gap);
		for (int i = gap; i < num + gap; i++) {
			double hue = from.getHue() + i * diff;
			hue %= 1.0;
			if (hue < 0) {
				hue += 1.0;
			}
			HLS hls = new HLS(hue, from.getLightness(), from.getSaturation());
			rainbow.add(hls);
		}
		return rainbow;
	}

	/**
	 * Returns a {@link Set} of evenly distributed {@link RGB}s creating a
	 * partial rainbow using the given rainbow and the selected {@link RGB}.
	 *
	 * @param num
	 * @param colors
	 * @param index
	 * @return
	 */
	@SuppressWarnings("serial")
	public static List<HLS> rainbow(int num, List<HLS> colors, int index) {
		Assert.isLegal(num >= 0);
		Assert.isLegal(index >= 0 && index < colors.size());
		colors = new ArrayList<HLS>(colors) {
			@Override
			public HLS get(int index) {
				int indexToGet = index % this.size();
				if (indexToGet < 0) {
					indexToGet += this.size();
				}
				return super.get(indexToGet);
			}
		};

		List<HLS> rainbow = new ArrayList<>();
		HLS left = colors.get(index - 1);
		HLS center = colors.get(index);
		HLS right = colors.get(index + 1);

		double leftSpan = MathUtils.angDiff(center.getHue(), left.getHue()) / 2.0;
		if (leftSpan == .0) {
			leftSpan = 1.0;
		}
		double rightSpan = MathUtils.angDiff(right.getHue(), center.getHue()) / 2.0;
		if (rightSpan == .0) {
			rightSpan = 1.0;
		}
		double span = leftSpan + rightSpan;
		if (span == .0) {
			span = 1.0;
		}
		double diff = span / num;

		double baseHue = center.getHue() - leftSpan + diff / 2.0;

		for (int i = 0; i < num; i++) {
			double hue = baseHue + i * diff;
			hue %= 1.0;
			if (hue < 0) {
				hue += 1.0;
			}
			HLS hls = new HLS(hue, center.getLightness(),
					center.getSaturation());
			rainbow.add(hls);
		}
		return rainbow;
	}

	/**
	 * Creates a new random {@link Color}.
	 * <p>
	 * <strong>You have to dispose this {@link Color} if you don't need it
	 * anymore.
	 *
	 * @return
	 */
	public static Color createRandomColor() {
		return new Color(Display.getCurrent(), ColorUtils.getRandomRGB()
				.toClassicRGB());
	}

	/**
	 * Generates a nice color
	 *
	 * @param hue
	 *            the color's hue (must be between 0 and 1)
	 * @return
	 */
	public static HLS getNiceHLS(double hue) {
		return new HLS(hue, 0.5, 0.55);
	}

	/**
	 * Creates a new nice {@link Color}.
	 * <p>
	 * <strong>You have to dispose this {@link Color} if you don't need it
	 * anymore.
	 *
	 * @param hue
	 *            the color's hue (must be between 0 and 1)
	 * @return
	 */
	public static Color createNiceColor(double hue) {
		return new Color(Display.getCurrent(), ColorSpaceConverter.HLStoRGB(
				getNiceHLS(hue)).toClassicRGB());
	}

	/**
	 * This method generates a color with the biggest possible distance to the
	 * given colors concerning the hue (meaning saturation and lightness are not
	 * considered).
	 * <p>
	 * e.g. if you have red and blue this method will return a greenish yellow.
	 *
	 * @return random color if null or an empty list is passed
	 */
	public static HLS getBestComplementColorHLS(List<HLS> colors) {
		// 0 different colors
		if (colors == null || colors.size() == 0) {
			return getRandomHLS();
		}

		// 1 different color -> complement
		List<Double> hues = new LinkedList<Double>();
		for (HLS color : colors) {
			double hue = color.getHue() % 1;
			if (!hues.contains(hue)) {
				hues.add(hue);
			}
		}
		if (hues.size() == 1) {
			return new HLS((colors.get(0).getHue() + 0.5) % 1, colors.get(0)
					.getLightness(), colors.get(0).getSaturation());
		}

		// > 1 different colors
		List<HLS> hlss = new ArrayList<HLS>(colors);
		Collections.sort(hlss,
				(o1, o2) -> new Double(o1.getHue()).compareTo(o2.getHue()));

		double maxDistance = 0;
		HLS prevHLS = hlss.get(hlss.size() - 1);
		HLS spaceStart = hlss.get(0);
		HLS spaceEnd = hlss.get(1);
		for (int i = 0; i < hlss.size(); i++) {
			HLS hls = hlss.get(i);
			double distance = hls.getHue() - prevHLS.getHue();
			distance += 1;
			distance %= 1; // we stay between 0 and 1, needed if lowest and
							// highest elements are compared in the first loop
			if (distance > maxDistance) {
				maxDistance = distance;
				spaceStart = prevHLS;
				spaceEnd = hls;
			}
			prevHLS = hls;
		}
		return new HLS((spaceStart.getHue() + maxDistance / 2) % 1,
				(spaceEnd.getLightness() + spaceStart.getLightness()) / 2,
				(spaceEnd.getSaturation() + spaceStart.getSaturation()) / 2);
	}

	/**
	 * This method generates a color with the biggest possible distance to the
	 * given colors concerning the hue (meaning saturation and lightness are not
	 * considered).
	 * <p>
	 * e.g. if you have red and blue this method will return a greenish yellow.
	 *
	 * @return random color if null or an empty list is passed
	 */
	public static RGB getBestComplementColor(Set<RGB> rgbs) {
		List<HLS> hlss = new ArrayList<HLS>(rgbs.size() + 2);
		for (RGB rgb : rgbs) {
			hlss.add(ColorSpaceConverter.RGBtoHLS(rgb));
		}
		HLS hls = getBestComplementColorHLS(hlss);
		return ColorSpaceConverter.HLStoRGB(hls);
	}
}
