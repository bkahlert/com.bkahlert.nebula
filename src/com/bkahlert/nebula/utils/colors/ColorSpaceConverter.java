/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) 2005-2008 Springer-Verlag Berlin,
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 *
 * Date: 2007/11/10
 *
 * This software is released under the terms of the GNU Lesser General Public License (LGPL).
 * http://imagingbook.com/index.php?id=98
 */

package com.bkahlert.nebula.utils.colors;

/**
 * Methods for converting between RGB and HLS color spaces.
 */

public class ColorSpaceConverter {

	public static HLS RGBtoHLS(RGB rgb) {
		// R,G,B assumed to be in [0,1]
		double cHi = Math.max(rgb.getRed(),
				Math.max(rgb.getGreen(), rgb.getBlue())); // highest color value
		double cLo = Math.min(rgb.getRed(),
				Math.min(rgb.getGreen(), rgb.getBlue())); // lowest color value
		double cRng = cHi - cLo; // color range

		// compute lightness L
		double L = (cHi + cLo) / 2;

		// compute saturation S
		double S = 0;
		if (0 < L && L < 1) {
			double d = (L <= 0.5f) ? L : (1 - L);
			S = 0.5f * cRng / d;
		}

		// compute hue H
		double H = 0;
		if (cHi > 0 && cRng > 0) { // a color pixel
			double rr = (cHi - rgb.getRed()) / cRng;
			double gg = (cHi - rgb.getGreen()) / cRng;
			double bb = (cHi - rgb.getBlue()) / cRng;
			double hh;
			if (rgb.getRed() == cHi) {
				hh = bb - gg;
			} else if (rgb.getGreen() == cHi) {
				hh = rr - bb + 2.0f;
			} else {
				// b is highest color value
				hh = gg - rr + 4.0f;
			}

			if (hh < 0) {
				hh = hh + 6;
			}
			H = hh / 6;
		}

		return new HLS(H, L, S, rgb.getAlpha());
	}

	public static RGB HLStoRGB(HLS hls) {
		double R = 0;
		// H,L,S assumed to be in [0,1]
		double G = 0, B = 0;

		if (hls.getLightness() <= 0) {
			R = G = B = 0;
		} else if (hls.getLightness() >= 1) {
			R = G = B = 1;
		} else {
			double hh = (6 * hls.getHue()) % 6;
			int c1 = (int) hh;
			double c2 = hh - c1;
			double d = (hls.getLightness() <= 0.5f) ? (hls.getSaturation() * hls
					.getLightness()) : (hls.getSaturation() * (1 - hls
					.getLightness()));
			double w = hls.getLightness() + d;
			double x = hls.getLightness() - d;
			double y = w - (w - x) * c2;
			double z = x + (w - x) * c2;
			switch (c1) {
			case 0:
				R = w;
				G = z;
				B = x;
				break;
			case 1:
				R = y;
				G = w;
				B = x;
				break;
			case 2:
				R = x;
				G = w;
				B = z;
				break;
			case 3:
				R = x;
				G = y;
				B = w;
				break;
			case 4:
				R = z;
				G = x;
				B = w;
				break;
			case 5:
				R = w;
				G = x;
				B = y;
				break;
			}
		}
		return new RGB(R, G, B, hls.getAlpha());
	}

	public static float[] RGBtoHSV(int R, int G, int B, float[] HSV) {
		// R,G,B in [0,255]
		float H = 0, S = 0, V = 0;
		float cMax = 255.0f;
		int cHi = Math.max(R, Math.max(G, B)); // highest color value
		int cLo = Math.min(R, Math.min(G, B)); // lowest color value
		int cRng = cHi - cLo; // color range

		// compute value V
		V = cHi / cMax;

		// compute saturation S
		if (cHi > 0) {
			S = (float) cRng / cHi;
		}

		// compute hue H
		if (cRng > 0) { // hue is defined only for color pixels
			float rr = (float) (cHi - R) / cRng;
			float gg = (float) (cHi - G) / cRng;
			float bb = (float) (cHi - B) / cRng;
			float hh;
			if (R == cHi) {
				hh = bb - gg;
			} else if (G == cHi) {
				hh = rr - bb + 2.0f;
			} else {
				// b is highest color value
				hh = gg - rr + 4.0f;
			}
			if (hh < 0) {
				hh = hh + 6;
			}
			H = hh / 6;
		}

		if (HSV == null) {
			HSV = new float[3];
		}
		HSV[0] = H;
		HSV[1] = S;
		HSV[2] = V;
		return HSV;
	}

	public static int HSVtoRGB(float h, float s, float v) {
		// h,s,v in [0,1]
		float rr = 0, gg = 0, bb = 0;
		float hh = (6 * h) % 6;
		int c1 = (int) hh;
		float c2 = hh - c1;
		float x = (1 - s) * v;
		float y = (1 - (s * c2)) * v;
		float z = (1 - (s * (1 - c2))) * v;
		switch (c1) {
		case 0:
			rr = v;
			gg = z;
			bb = x;
			break;
		case 1:
			rr = y;
			gg = v;
			bb = x;
			break;
		case 2:
			rr = x;
			gg = v;
			bb = z;
			break;
		case 3:
			rr = x;
			gg = y;
			bb = v;
			break;
		case 4:
			rr = z;
			gg = x;
			bb = v;
			break;
		case 5:
			rr = v;
			gg = x;
			bb = y;
			break;
		}
		int N = 256;
		int r = Math.min(Math.round(rr * N), N - 1);
		int g = Math.min(Math.round(gg * N), N - 1);
		int b = Math.min(Math.round(bb * N), N - 1);
		// create int-packed RGB-color:
		int rgb = ((r & 0xff) << 16) | ((g & 0xff) << 8) | b & 0xff;
		return rgb;
	}

}
