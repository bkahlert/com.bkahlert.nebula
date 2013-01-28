package com.bkahlert.devel.nebula.rendering;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// TODO: handle null for start or end, too
public class TrackCalculator {

	public static interface ITrackCalculation<T> extends Iterable<T> {
		/**
		 * Returns the number of the track of a given item.
		 * 
		 * @param item
		 * @return <code>null</code> if item is not in the calculation
		 */
		public Integer getTrack(T item);

		/**
		 * Returns the number of used tracks on the given segment.
		 * <p>
		 * 
		 * <pre>
		 * Track #  01234...
		 *          |
		 * item1 -> || <- item2
		 *          |
		 *          ||
		 *          |||
		 *          |||
		 *          ||
		 *           |
		 * </pre>
		 * 
		 * In this example the number of tracks in the second line is 2 for item
		 * 1 and 2.
		 * 
		 * @param item
		 * @return
		 */
		public Integer getNumTracks(T item);

		/**
		 * Return the number of items contained in the {@link ITrackCalculation}
		 * .
		 * <p>
		 * 
		 * <pre>
		 * Track # 01234...
		 *         |
		 *         ||
		 *         |
		 *         ||
		 *         |||
		 *         |||
		 *         ||
		 *          |
		 * </pre>
		 * 
		 * In this example 4 items occur.
		 * 
		 * @return
		 */
		public int getItemCount();

		/**
		 * Returns the number of tracks needed at most.
		 * <p>
		 * Alternately this is the number of items that occur at most in
		 * parallel.
		 * <p>
		 * 
		 * <pre>
		 * Track # 01234...
		 *         |
		 *         ||
		 *         |
		 *         ||
		 *         |||
		 *         |||
		 *         ||
		 *          |
		 * </pre>
		 * 
		 * In this example the max items in parallel is 3.
		 * 
		 * @return
		 */
		public int getMaxTracks();
	}

	private static class TrackCalculation<T> implements ITrackCalculation<T> {

		private Map<T, Integer> tracks;
		private Map<T, Integer> numTracks;
		private int itemCount;
		private int maxTrackNumber;

		public TrackCalculation(Map<T, Integer> tracks,
				Map<T, Integer> numTracks) {
			this.tracks = tracks;
			this.numTracks = numTracks;
			this.itemCount = tracks.keySet().size();
			this.maxTrackNumber = 0;
			for (int trackNumber : tracks.values()) {
				if (trackNumber > this.maxTrackNumber)
					this.maxTrackNumber = trackNumber;
			}
		}

		@Override
		public Integer getTrack(T item) {
			return this.tracks.get(item);
		}

		public Integer getNumTracks(T item) {
			return this.numTracks.get(item);
		};

		@Override
		public int getItemCount() {
			return this.itemCount;
		}

		@Override
		public int getMaxTracks() {
			return this.maxTrackNumber + 1;
		}

		@Override
		public Iterator<T> iterator() {
			return this.tracks.keySet().iterator();
		}

	}

	public static interface Converter<T> {
		public Long getStart(T item);

		public Long getEnd(T item);
	}

	public static <T> ITrackCalculation<T> calculateTracks(List<T> items,
			final Converter<T> converter) {
		assert converter != null;
		Map<T, Integer> tracks = new HashMap<T, Integer>();
		Map<T, Integer> numTracks = new HashMap<T, Integer>();
		if (items == null)
			return new TrackCalculation<T>(tracks, numTracks);
		Collections.sort(items, new Comparator<T>() {
			@Override
			public int compare(T item1, T item2) {
				int rt = 0;
				if (converter.getStart(item1) == null) {
					if (converter.getStart(item2) == null)
						rt = 0;
					else
						rt = 1;
				} else {
					rt = converter.getStart(item1).compareTo(
							converter.getStart(item2));
				}

				return rt;
			}
		});
		for (T item : items) {
			if (converter.getStart(item) > converter.getEnd(item))
				throw new IllegalArgumentException(
						"Item's start must be before its end");

			numTracks.put(item, 1);
			List<Integer> blockedTracks = new LinkedList<Integer>();
			for (T processedItem : tracks.keySet()) {
				int track = tracks.get(processedItem);
				if (blockedTracks.contains(track))
					continue;
				if (collide(converter.getStart(item), converter.getEnd(item),
						converter.getStart(processedItem),
						converter.getEnd(processedItem))) {
					blockedTracks.add(track);
					numTracks.put(item, numTracks.get(item) + 1);
					numTracks.put(processedItem,
							numTracks.get(processedItem) + 1);
				}
			}

			int i = 0;
			for (; i < Integer.MAX_VALUE; i++)
				if (!blockedTracks.contains(i))
					break;
			tracks.put(item, i);
		}

		return new TrackCalculation<T>(tracks, numTracks);
	}

	public static boolean collide(Long item1Start, Long item1End,
			Long item2Start, Long item2End) {
		if (item1End.compareTo(item2Start) <= 0)
			return false;
		if (item1Start.compareTo(item2End) >= 0)
			return false;
		return true;
	}
}
