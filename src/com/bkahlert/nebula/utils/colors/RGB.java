package com.bkahlert.nebula.utils.colors;

import org.apache.commons.lang.StringUtils;

import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;

/**
 * Instances of this class describe colors in the RGB (red, green, blue) color
 * space.
 * <p>
 * In contrast to {@link org.eclipse.swt.graphics.RGB} this class uses
 * {@link Double}s to save its colors. This allows for a much higher color
 * resolution.
 * 
 * @author bkahlert
 */
public class RGB {
	public static final RGB WHITE = new RGB(1.0, 1.0, 1.0);
	public static final RGB BLACK = new RGB(0.0, 0.0, 0.0);

	public static final RGB PRIMARY = BootstrapBrowser.ButtonOption.PRIMARY
			.getColor();
	public static final RGB SUCCESS = BootstrapBrowser.ButtonOption.SUCCESS
			.getColor();
	public static final RGB INFO = BootstrapBrowser.ButtonOption.INFO
			.getColor();
	public static final RGB WARNING = BootstrapBrowser.ButtonOption.WARNING
			.getColor();
	public static final RGB DANGER = BootstrapBrowser.ButtonOption.DANGER
			.getColor();

	public static final RGB IMPORTANCE_HIGH = new RGB(127, 64, 195);
	public static final RGB IMPORTANCE_LOW = new RGB(179, 179, 179);

	private double red;
	private double green;
	private double blue;
	private double alpha;

	/**
	 * Constructs a new RGB.
	 * 
	 * @param red
	 *            0.0 ... 1.0
	 * @param green
	 *            0.0 ... 1.0
	 * @param blue
	 *            0.0 ... 1.0
	 */
	public RGB(double red, double green, double blue) {
		this(red, green, blue, 1.0);
	}

	/**
	 * Constructs a new RGB.
	 * 
	 * @param red
	 *            0.0 ... 1.0
	 * @param green
	 *            0.0 ... 1.0
	 * @param blue
	 *            0.0 ... 1.0
	 * @param alpha
	 *            0.0 ... 1.0
	 */
	public RGB(double red, double green, double blue, double alpha) {
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		this.setAlpha(alpha);
	}

	/**
	 * Constructs a new RGB
	 * 
	 * @param red
	 *            0 ... 255
	 * @param green
	 *            0 ... 255
	 * @param blue
	 *            0 ... 255
	 */
	public RGB(int red, int green, int blue) {
		this(red, green, blue, 255);
	}

	/**
	 * Constructs a new RGB
	 * 
	 * @param red
	 *            0 ... 255
	 * @param green
	 *            0 ... 255
	 * @param blue
	 *            0 ... 255
	 * @param alpha
	 *            0 ... 255
	 */
	public RGB(int red, int green, int blue, int alpha) {
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		this.setAlpha(alpha);
	}

	public RGB(org.eclipse.swt.graphics.RGB rgb) {
		this((double) rgb.red / 255, (double) rgb.green / 255,
				(double) rgb.blue / 255);
	}

	public org.eclipse.swt.graphics.RGB toClassicRGB() {
		return new org.eclipse.swt.graphics.RGB(
				(int) Math.round(this.red * 255),
				(int) Math.round(this.green * 255),
				(int) Math.round(this.blue * 255));
	}

	/**
	 * @return red 0.0 ... 1.0
	 */
	public double getRed() {
		return this.red;
	}

	/**
	 * @param red
	 *            0.0 ... 1.0
	 */
	public void setRed(double red) {
		if (red < 0 || red > 1) {
			throw new IllegalArgumentException("Red must be between 0 and 1");
		}
		this.red = red;
	}

	/**
	 * @param red
	 *            0 ... 255
	 */
	public void setRed(int red) {
		if (red < 0) {
			red = 0;
		}
		if (red > 255) {
			red = 255;
		}
		this.setRed(red / 255.0);
	}

	/**
	 * @return green 0.0 ... 1.0
	 */
	public double getGreen() {
		return this.green;
	}

	/**
	 * @param green
	 *            0.0 ... 1.0
	 */
	public void setGreen(double green) {
		if (green < 0 || green > 1) {
			throw new IllegalArgumentException("Green must be between 0 and 1");
		}
		this.green = green;
	}

	/**
	 * @param red
	 *            0 ... 255
	 */
	public void setGreen(int green) {
		if (green < 0) {
			green = 0;
		}
		if (green > 255) {
			green = 255;
		}
		this.setGreen(green / 255.0);
	}

	/**
	 * @return blue 0.0 ... 1.0
	 */
	public double getBlue() {
		return this.blue;
	}

	/**
	 * @param blue
	 *            0.0 ... 1.0
	 */
	public void setBlue(double blue) {
		if (blue < 0 || blue > 1) {
			throw new IllegalArgumentException("Blue must be between 0 and 1");
		}
		this.blue = blue;
	}

	/**
	 * @param blue
	 *            0 ... 255
	 */
	public void setBlue(int blue) {
		if (blue < 0) {
			blue = 0;
		}
		if (blue > 255) {
			blue = 255;
		}
		this.setBlue(blue / 255.0);
	}

	/**
	 * @return alpha 0.0 ... 1.0
	 */
	public double getAlpha() {
		return this.alpha;
	}

	/**
	 * @param alpha
	 *            0.0 ... 1.0
	 */
	public void setAlpha(double alpha) {
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException("Alpha must be between 0 and 1");
		}
		this.alpha = alpha;
	}

	/**
	 * @param red
	 *            0 ... 255
	 */
	public void setAlpha(int alpha) {
		if (alpha < 0) {
			alpha = 0;
		}
		if (alpha > 255) {
			alpha = 255;
		}
		this.setAlpha(alpha / 255.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.blue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.green);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.red);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RGB)) {
			return false;
		}
		RGB other = (RGB) obj;
		if (Double.doubleToLongBits(this.blue) != Double
				.doubleToLongBits(other.blue)) {
			return false;
		}
		if (Double.doubleToLongBits(this.green) != Double
				.doubleToLongBits(other.green)) {
			return false;
		}
		if (Double.doubleToLongBits(this.red) != Double
				.doubleToLongBits(other.red)) {
			return false;
		}
		return true;
	}

	public String toHexString() {
		StringBuilder sb = new StringBuilder("#");
		sb.append(StringUtils.leftPad(
				Integer.toHexString((int) Math.round(this.getRed() * 255))
						.toUpperCase(), 2, '0'));
		sb.append(StringUtils.leftPad(
				Integer.toHexString((int) Math.round(this.getGreen() * 255))
						.toUpperCase(), 2, '0'));
		sb.append(StringUtils.leftPad(
				Integer.toHexString((int) Math.round(this.getBlue() * 255))
						.toUpperCase(), 2, '0'));
		if (this.alpha < 1.0) {
			sb.append(StringUtils.leftPad(
					Integer.toHexString((int) Math.round(this.getAlpha() * 255))
							.toUpperCase(), 2, '0'));
		}
		return sb.toString();
	}

	public String toCssString() {
		StringBuilder sb = new StringBuilder();
		if (this.alpha == 1.0) {
			sb.append("rgb(");
		} else {
			sb.append("rgba(");
		}
		sb.append((int) Math.round(this.getRed() * 255));
		sb.append(", ");
		sb.append((int) Math.round(this.getGreen() * 255));
		sb.append(", ");
		sb.append((int) Math.round(this.getBlue() * 255));
		if (this.alpha != 1.0) {
			sb.append(", ");
			sb.append(this.alpha);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String toString() {
		HLS hls = ColorSpaceConverter.RGBtoHLS(this);
		return this.toHexString() + " (" + hls.getDecString() + ")";
	}
}
