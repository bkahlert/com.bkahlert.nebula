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
		return this.hue;
	}

	/**
	 * @param hue
	 */
	public void setHue(double hue) {
		if (hue < 0 || hue > 1) {
			throw new IllegalArgumentException("Hue must be between 0 and 1");
		}
		this.hue = hue;
	}

	/**
	 * @return lightness
	 */
	public double getLightness() {
		return this.lightness;
	}

	/**
	 * @param lightness
	 */
	public void setLightness(double lightness) {
		if (lightness < 0 || lightness > 1) {
			throw new IllegalArgumentException(
					"Lightness must be between 0 and 1");
		}
		this.lightness = lightness;
	}

	/**
	 * @return saturation
	 */
	public double getSaturation() {
		return this.saturation;
	}

	/**
	 * @param saturation
	 */
	public void setSaturation(double saturation) {
		if (saturation < 0 || saturation > 1) {
			throw new IllegalArgumentException(
					"Saturation must be between 0 and 1");
		}
		this.saturation = saturation;
	}

	/**
	 * Mixes the given {@link RGB} with the current {@link RGB} and returns the
	 * resulting {@link RGB}. This instance stays untouched.
	 * 
	 * <p>
	 * The mixing does not consider alpha transparency! The returned color is
	 * always fully opaque.
	 * 
	 * @param weight
	 * 
	 * @param rgb
	 * @return
	 */
	public HLS mix(HLS hls, double weight) {
		// additive mixing(?)
		// double saturation = 0.5 * (this.getSaturation() +
		// hls2.getSaturation());
		// double lightness = 0.5 * (this.getLightness() + hls2.getLightness());
		// double x = Math.cos(2.0 * Math.PI * this.getHue())
		// + Math.cos(2.0 * Math.PI * hls2.getHue());
		// double y = Math.sin(2.0 * Math.PI * this.getHue())
		// + Math.sin(2.0 * Math.PI * hls2.getHue());
		// double hue;
		// if (x != 0.0 || y != 0.0) {
		// hue = Math.atan2(y, x) / (2 * Math.PI);
		// } else {
		// hue = 0.0;
		// saturation = 0.0;
		// }
		// if (hue < 0) {
		// hue += 1.0;
		// }

		return ColorSpaceConverter.RGBtoHLS(ColorSpaceConverter.HLStoRGB(this)
				.mix(ColorSpaceConverter.HLStoRGB(hls), weight));
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
		temp = Double.doubleToLongBits(this.hue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.lightness);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.saturation);
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
		if (!(obj instanceof HLS)) {
			return false;
		}
		HLS other = (HLS) obj;
		if (Double.doubleToLongBits(this.hue) != Double
				.doubleToLongBits(other.hue)) {
			return false;
		}
		if (Double.doubleToLongBits(this.lightness) != Double
				.doubleToLongBits(other.lightness)) {
			return false;
		}
		if (Double.doubleToLongBits(this.saturation) != Double
				.doubleToLongBits(other.saturation)) {
			return false;
		}
		return true;
	}

	public String getDecString() {
		return HLS.class.getSimpleName() + "(" + this.getHue() * 360 + "�, "
				+ Math.round(this.getLightness() * 100) + "%, "
				+ Math.round(this.getSaturation() * 100) + "%)";
	}

	@Override
	public String toString() {
		RGB rgb = ColorSpaceConverter.HLStoRGB(this);
		return this.getDecString() + ", (" + rgb.toHexString() + ")";
	}
}
