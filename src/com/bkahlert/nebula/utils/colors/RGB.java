package com.bkahlert.nebula.utils.colors;

import org.apache.commons.lang.StringUtils;

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

	public static final RGB PRIMARY = new RGB(50, 118, 177);
	public static final RGB SUCCESS = new RGB(71, 164, 71);
	public static final RGB INFO = new RGB(57, 179, 215);
	public static final RGB WARNING = new RGB(237, 156, 40);
	public static final RGB DANGER = new RGB(210, 50, 45);

	private double red;
	private double green;
	private double blue;

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
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
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
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
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
	 * @param red
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
		return "#"
				+ StringUtils.leftPad(
						Integer.toHexString(
								(int) Math.round(this.getRed() * 255))
								.toUpperCase(), 2, '0')
				+ StringUtils.leftPad(
						Integer.toHexString(
								(int) Math.round(this.getGreen() * 255))
								.toUpperCase(), 2, '0')
				+ StringUtils.leftPad(
						Integer.toHexString(
								(int) Math.round(this.getBlue() * 255))
								.toUpperCase(), 2, '0');
	}

	@Override
	public String toString() {
		HLS hls = ColorSpaceConverter.RGBtoHLS(this);
		return this.toHexString() + " (" + hls.getDecString() + ")";
	}
}
