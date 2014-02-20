package com.bkahlert.nebula.utils.colors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.HLS;
import com.bkahlert.nebula.utils.colors.RGB;

;

public class ColorUtilsTest {

	private RGB colorBlack;
	private RGB colorWhite;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.colorBlack = new RGB(0, 0, 0);
		this.colorWhite = new RGB(1, 1, 1);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * TimelineViewer method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test
	public void testAddLightnessRGBFloat() {
		RGB colorTest = new RGB(0.5, 0.5, 0.5);
		RGB shouldBeWhite = ColorUtils.addLightness(colorTest, +1);
		RGB shouldBeBlack = ColorUtils.addLightness(colorTest, -1);
		RGB shouldBeSame = ColorUtils.addLightness(colorTest, 0);

		assertTrue("should be White", shouldBeWhite.equals(this.colorWhite));
		assertTrue("should be Black", shouldBeBlack.equals(this.colorBlack));
		assertTrue("should be Same", shouldBeSame.equals(colorTest));
	}

	/**
	 * TimelineViewer method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testAddLightnessRGBFloat2() {
		ColorUtils.addLightness(new RGB(0.5, 0.5, 0.5), -2);
	}

	/**
	 * TimelineViewer method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#addLightness(org.eclipse.swt.graphics.Color, float)}
	 * .
	 */
	@Test
	public void testAddLightnessColorFloat() {
		// TODO
		// how to get device?
		// its the same like RGB testing
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleLightnessWithInvalidRange() {
		ColorUtils.scaleLightnessBy(new RGB(0.5, 0.5, 0.5), -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testScaleSaturationWithInvalidRange() {
		ColorUtils.scaleSaturationBy(new RGB(0.5, 0.5, 0.5), -1);
	}

	/**
	 * TimelineViewer method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.RGB, float)}
	 * .
	 */
	@Test
	public void testScaleColorByRGBFloat() {

		RGB colorTest = new RGB(0.5, 0.5, 0.5);
		RGB shouldBeBlack = ColorUtils.scaleLightnessBy(colorTest, 0);
		RGB shouldBeSame = ColorUtils.scaleLightnessBy(colorTest, 1);
		RGB shouldBeBrighter = ColorUtils.scaleLightnessBy(colorTest, 2);

		assertTrue("should be Black", shouldBeBlack.equals(this.colorBlack));
		assertTrue("should be the Same", shouldBeSame.equals(colorTest));
		assertTrue("should be brighter",
				shouldBeBrighter.toClassicRGB().red > 128);
	}

	/**
	 * TimelineViewer method for
	 * {@link de.fu_berlin.inf.dpp.util.ColorUtils#scaleColorBy(org.eclipse.swt.graphics.Color, float)}
	 * .
	 */
	@Test
	public void testScaleColorByColorFloat() {
		// TODO
		// how to get device?
		// its the same like RGB testing
	}

	@Test
	public void testGetBestComplementColorHLS() {
		assertNotNull(ColorUtils.getBestComplementColorHLS(null));
		assertNotNull(ColorUtils
				.getBestComplementColorHLS(new LinkedList<HLS>()));

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0, 0.5, 0.5),
								new HLS(1, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(1, 0.5, 0.5),
								new HLS(1, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0, 0.5, 0.5),
								new HLS(0, 0.5, 0.5))).getHue(), 0.05);

		assertEquals(
				0.5,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0.2, 0.5, 0.5), new HLS(0.8, 0.5,
								0.5))).getHue(), 0.05);

		assertEquals(
				0.8,
				ColorUtils.getBestComplementColorHLS(
						Arrays.asList(new HLS(0.2, 0.5, 0.5), new HLS(0.4, 0.5,
								0.5))).getHue(), 0.05);

	}
}
