package com.bkahlert.nebula.rendering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.bkahlert.nebula.rendering.TrackCalculator;
import com.bkahlert.nebula.rendering.TrackCalculator.Converter;
import com.bkahlert.nebula.rendering.TrackCalculator.ITrackCalculation;

public class TrackCalculatorTest {

	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public Long getStart(String item) {
			String[] parts = item.split(" ");
			return Long.parseLong(parts[0]);
		}

		@Override
		public Long getEnd(String item) {
			String[] parts = item.split(" ");
			return Long.parseLong(parts[1]);
		}
	};

	private <T> List<T> convert(T... ts) {
		return Arrays.asList(ts);
	}

	@Test
	public void testCollide() {
		assertFalse(TrackCalculator.collide(10l, 20l, 30l, 40l));
		assertTrue(TrackCalculator.collide(10l, 20l, 15l, 40l));
		assertFalse(TrackCalculator.collide(30l, 40l, 10l, 20l));
		assertTrue(TrackCalculator.collide(15l, 40l, 10l, 20l));
	}

	@Test
	public <T> void testNull() {
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				null, STRING_CONVERTER);
		assertEquals(0, tracks.getItemCount());
		assertEquals(1, tracks.getMaxTracks());
	}

	@Test
	public void testZeroElements() {
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				new LinkedList<String>(), STRING_CONVERTER);
		assertEquals(0, tracks.getItemCount());
		assertEquals(1, tracks.getMaxTracks());
	}

	@Test
	public void testOneElement() {
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				convert("20 50"), STRING_CONVERTER);
		assertEquals(1, tracks.getItemCount());
		assertEquals(1, tracks.getMaxTracks());

		String item = tracks.iterator().next();
		assertEquals(0, (int) tracks.getTrack(item));
		assertEquals(1, (int) tracks.getNumTracks(item));
	}

	@Test
	public void testTwoElements() {
		String item1 = "15 40";
		String item2 = "20 50";
		String item3 = "200 250";

		// item1, item2
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				convert(item1, item2), STRING_CONVERTER);
		assertEquals(2, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		Iterator<String> i = tracks.iterator();
		String trackItem1 = i.next();
		assertEquals(item1, trackItem1);
		assertEquals(0, (int) tracks.getTrack(trackItem1));
		assertEquals(2, (int) tracks.getNumTracks(trackItem1));
		String trackItem2 = i.next();
		assertEquals(item2, trackItem2);
		assertEquals(1, (int) tracks.getTrack(trackItem2));
		assertEquals(2, (int) tracks.getNumTracks(trackItem2));

		// item2, item1
		tracks = TrackCalculator.calculateTracks(convert(item2, item1),
				STRING_CONVERTER);
		assertEquals(2, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		i = tracks.iterator();
		trackItem1 = i.next();
		assertEquals(item1, trackItem1);
		assertEquals(0, (int) tracks.getTrack(trackItem1));
		assertEquals(2, (int) tracks.getNumTracks(trackItem1));
		trackItem2 = i.next();
		assertEquals(item2, trackItem2);
		assertEquals(1, (int) tracks.getTrack(trackItem2));
		assertEquals(2, (int) tracks.getNumTracks(trackItem2));

		// item2, item1, item3
		tracks = TrackCalculator.calculateTracks(convert(item2, item1, item3),
				STRING_CONVERTER);
		assertEquals(3, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		i = tracks.iterator();
		trackItem1 = i.next();
		assertEquals(item1, trackItem1);
		assertEquals(0, (int) tracks.getTrack(trackItem1));
		assertEquals(2, (int) tracks.getNumTracks(trackItem1));
		trackItem2 = i.next();
		assertEquals(item2, trackItem2);
		assertEquals(1, (int) tracks.getTrack(trackItem2));
		assertEquals(2, (int) tracks.getNumTracks(trackItem2));
		String trackItem3 = i.next();
		assertEquals(item3, trackItem3);
		assertEquals(0, (int) tracks.getTrack(trackItem3));
		assertEquals(1, (int) tracks.getNumTracks(trackItem3));
	}

	@Test
	public void testTwoSameStartElements() {
		String item1 = "15 60";
		String item2 = "15 50";
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				convert(item1, item2), STRING_CONVERTER);
		assertEquals(2, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		Iterator<String> i = tracks.iterator();
		String trackItem1 = i.next();
		assertTrue(trackItem1.equals(item1) || trackItem1.equals(item2));
		assertTrue(trackItem1.equals("15 60") || trackItem1.equals("15 50"));
		assertEquals(0, (int) tracks.getTrack(trackItem1));
		assertEquals(2, (int) tracks.getNumTracks(trackItem1));

		String trackItem2 = i.next();
		assertTrue(trackItem2.equals(item1) || trackItem2.equals(item2));
		assertTrue(trackItem2.equals("15 60") || trackItem2.equals("15 50"));
		assertEquals(1, (int) tracks.getTrack(trackItem2));
		assertEquals(2, (int) tracks.getNumTracks(trackItem2));

		assertTrue(((int) tracks.getTrack("15 60")) == 0
				|| ((int) tracks.getTrack("15 60")) == 1);
		assertTrue(((int) tracks.getTrack("15 50")) == 0
				|| ((int) tracks.getTrack("15 50")) == 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidElement() {
		TrackCalculator.calculateTracks(convert("15 40", "2000000 50"),
				STRING_CONVERTER);
	}

	@Test
	public void testConvertingTwoElements() {
		String item1 = "20 50";
		String item2 = "15 50";
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				convert(item1, item2), STRING_CONVERTER);
		assertEquals(2, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		Iterator<String> i = tracks.iterator();
		String s1 = i.next();
		String s2 = i.next();
		if (item1.equals(s1)) {
			assertEquals("20 50", s1);
			assertEquals(1, (int) tracks.getTrack(s1));
			assertEquals(2, (int) tracks.getNumTracks(s1));
			assertEquals("15 50", s2);
			assertEquals(item2, s2);
			assertEquals(0, (int) tracks.getTrack(s2));
			assertEquals(2, (int) tracks.getNumTracks(s2));
		} else {
			assertEquals("15 50", s1);
			assertEquals(0, (int) tracks.getTrack(s1));
			assertEquals(2, (int) tracks.getNumTracks(s1));
			assertEquals("20 50", s2);
			assertEquals(item1, s2);
			assertEquals(1, (int) tracks.getTrack(s2));
			assertEquals(2, (int) tracks.getNumTracks(s2));
		}
	}

	@Test
	public void testConvertingTwoSameStartElements() {
		String item1 = "15 60";
		String item2 = "15 50";
		ITrackCalculation<String> tracks = TrackCalculator.calculateTracks(
				convert(item1, item2), STRING_CONVERTER);
		assertEquals(2, tracks.getItemCount());
		assertEquals(2, tracks.getMaxTracks());

		Iterator<String> i = tracks.iterator();
		String s1 = i.next();
		// we do not know the order since both elements are have the same start
		// date
		assertTrue(s1.equals(item1) || s1.equals(item2));
		assertTrue(s1.equals("15 60") || s1.equals("15 50"));
		assertEquals(0, (int) tracks.getTrack(s1));
		assertEquals(2, (int) tracks.getNumTracks(s1));

		String s2 = i.next();
		// we do not know the order since both elements are have the same start
		// date
		assertTrue(s2.equals(item1) || s2.equals(item2));
		assertTrue(s2.equals("15 60") || s2.equals("15 50"));
		assertEquals(1, (int) tracks.getTrack(s2));
		assertEquals(2, (int) tracks.getNumTracks(s2));

		assertTrue(((int) tracks.getTrack("15 60")) == 0
				|| ((int) tracks.getTrack("15 60")) == 1);
		assertTrue(((int) tracks.getTrack("15 50")) == 0
				|| ((int) tracks.getTrack("15 50")) == 1);
	}
}
