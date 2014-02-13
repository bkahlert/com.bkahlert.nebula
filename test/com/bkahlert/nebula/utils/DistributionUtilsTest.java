package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.RelativeWidth;
import com.bkahlert.nebula.utils.DistributionUtils.Width;

public class DistributionUtilsTest {

	private void assertEquals_(double[] a, double[] b) {
		assertEquals(a.length, b.length);
		for (int i = 0; i < a.length; i++) {
			assertTrue(a[i] + " != " + b[i], a[i] == b[i]);
		}
	}

	@Test
	public void testDistribute() {
		this.assertEquals_(new double[] {},
				DistributionUtils.distribute(new Width[] {}, 100));
		this.assertEquals_(new double[] { 30 }, DistributionUtils.distribute(
				new Width[] { new AbsoluteWidth(30) }, 100));
		this.assertEquals_(new double[] { 100 }, DistributionUtils.distribute(
				new Width[] { new RelativeWidth(1.0) }, 100));
		this.assertEquals_(
				new double[] { 30, 70 },
				DistributionUtils.distribute(new Width[] { new AbsoluteWidth(30),
						new RelativeWidth(1.0) }, 100));
		this.assertEquals_(
				new double[] { 30, 45, 25 },
				DistributionUtils.distribute(new Width[] { new AbsoluteWidth(30),
						new RelativeWidth(1.0), new AbsoluteWidth(25) }, 100));
		this.assertEquals_(
				new double[] { 0, 75, 25 },
				DistributionUtils.distribute(new Width[] { new AbsoluteWidth(0),
						new RelativeWidth(1.0), new AbsoluteWidth(25) }, 100));
		this.assertEquals_(
				new double[] { 30, 80, 25, 14 },
				DistributionUtils.distribute(new Width[] { new AbsoluteWidth(30),
						new RelativeWidth(.7, 80), new AbsoluteWidth(25),
						new RelativeWidth(.3) }, 100));
		this.assertEquals_(
				new double[] { 30, 0, 100, 17.5 },
				DistributionUtils.distribute(new Width[] { new AbsoluteWidth(30),
						new RelativeWidth(.7), new AbsoluteWidth(100),
						new RelativeWidth(.3, 17.5) }, 100));
	}

}
