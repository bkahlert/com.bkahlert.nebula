package com.bkahlert.devel.nebula.colors;

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
	private double red;
	private double green;
	private double blue;

	public RGB(double red, double green, double blue) {
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
	 * @return red
	 */
	public double getRed() {
		return this.red;
	}

	/**
	 * @param red
	 */
	public void setRed(double red) {
		if (red < 0 || red > 1) {
			throw new IllegalArgumentException("Red must be between 0 and 1");
		}
		this.red = red;
	}

	/**
	 * @return green
	 */
	public double getGreen() {
		return this.green;
	}

	/**
	 * @param green
	 */
	public void setGreen(double green) {
		if (green < 0 || green > 1) {
			throw new IllegalArgumentException("Green must be between 0 and 1");
		}
		this.green = green;
	}

	/**
	 * @return blue
	 */
	public double getBlue() {
		return this.blue;
	}

	/**
	 * @param blue
	 */
	public void setBlue(double blue) {
		if (blue < 0 || blue > 1) {
			throw new IllegalArgumentException("Blue must be between 0 and 1");
		}
		this.blue = blue;
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
