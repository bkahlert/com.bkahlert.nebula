package com.bkahlert.nebula.utils.colors;

/**
 * Instances of this class describe colors in the HLS (hue, saturation,
 * lightness) color space.
 * 
 * @author bkahlert
 */
public class HLS {
	private double hue;
	private double lightness;
	private double saturation;

	public HLS(double hue, double lightness, double saturation) {
		super();

		this.setHue(hue);
		this.setLightness(lightness);
		this.setSaturation(saturation);
	}

	/**
	 * @return hue
	 */
	public double getHue() {
		return hue;
	}

	/**
	 * @param hue
	 */
	public void setHue(double hue) {
		if (hue < 0 || hue > 1)
			throw new IllegalArgumentException("Hue must be between 0 and 1");
		this.hue = hue;
	}

	/**
	 * @return lightness
	 */
	public double getLightness() {
		return lightness;
	}

	/**
	 * @param lightness
	 */
	public void setLightness(double lightness) {
		if (lightness < 0 || lightness > 1)
			throw new IllegalArgumentException(
					"Lightness must be between 0 and 1");
		this.lightness = lightness;
	}

	/**
	 * @return saturation
	 */
	public double getSaturation() {
		return saturation;
	}

	/**
	 * @param saturation
	 */
	public void setSaturation(double saturation) {
		if (saturation < 0 || saturation > 1)
			throw new IllegalArgumentException(
					"Saturation must be between 0 and 1");
		this.saturation = saturation;
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
		temp = Double.doubleToLongBits(hue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lightness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(saturation);
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HLS))
			return false;
		HLS other = (HLS) obj;
		if (Double.doubleToLongBits(hue) != Double.doubleToLongBits(other.hue))
			return false;
		if (Double.doubleToLongBits(lightness) != Double
				.doubleToLongBits(other.lightness))
			return false;
		if (Double.doubleToLongBits(saturation) != Double
				.doubleToLongBits(other.saturation))
			return false;
		return true;
	}

	public String getDecString() {
		return HLS.class.getSimpleName() + "(" + getHue() * 360 + "�, "
				+ Math.round(getLightness() * 100) + "%, "
				+ Math.round(getSaturation() * 100) + "%)";
	}

	@Override
	public String toString() {
		RGB rgb = ColorSpaceConverter.HLStoRGB(this);
		return this.getDecString() + ", (" + rgb.toHexString() + ")";
	}
}
