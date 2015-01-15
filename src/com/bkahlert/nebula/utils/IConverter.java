package com.bkahlert.nebula.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * {@link IConverter} can convert an object of arbitrary type to type
 * <code>T</code>.
 *
 * @author bjornson
 *
 * @param <T>
 *            type give objects can be converted to
 */
public interface IConverter<SRC, DEST> {

	/**
	 * {@link IConverter} that converts objects to null of type {@link Void}.
	 */
	public static final IConverter<Object, Void> CONVERTER_VOID = returnValue -> null;

	/**
	 * {@link IConverter} that converts objects to {@link Boolean}s. Converts to
	 * <code>true</code> if the given object is of type {@link Boolean} and of
	 * value <code>true</code>. Otherwise converts to <code>false</code>.
	 */
	public static final IConverter<Object, Boolean> CONVERTER_BOOLEAN = returnValue -> {
		if (returnValue == null || !Boolean.class.isInstance(returnValue)) {
			return false;
		}
		return (Boolean) returnValue;
	};

	/**
	 * {@link IConverter} that converts objects to {@link Strings}s. Returns a
	 * {@link String} if the object is of type {@link String}. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, String> CONVERTER_STRING = returnValue -> {
		if (returnValue == null || !String.class.isInstance(returnValue)) {
			return null;
		}
		return (String) returnValue;
	};

	/**
	 * {@link IConverter} that converts objects a {@link List} of
	 * {@link Strings}s. If a primitive type is returned, a list containing this
	 * single element is returned. <code>null</code> is directly passed through.
	 */
	public static final IConverter<Object, List<String>> CONVERTER_STRINGLIST = returnValue -> {
		if (returnValue == null) {
			return null;
		} else if (Object[].class.isInstance(returnValue)) {
			List<String> strings = new LinkedList<String>();
			for (int i = 0, m = ((Object[]) returnValue).length; i < m; i++) {
				Object obj = ((Object[]) returnValue)[i];
				strings.add(obj != null ? obj.toString() : null);
			}
			return strings;
		} else {
			return Arrays.asList(returnValue.toString());
		}
	};

	/**
	 * {@link IConverter} that converts objects to {@link Point}s. Returns a
	 * {@link Point} if the object is an array of two {@link Double}s. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, Point> CONVERTER_POINT = returnValue -> {
		if (returnValue == null || !Object[].class.isInstance(returnValue)
				|| ((Object[]) returnValue).length != 2
				|| !Double.class.isInstance(((Object[]) returnValue)[0])
				|| !Double.class.isInstance(((Object[]) returnValue)[1])) {
			return null;
		}
		Object[] pos = (Object[]) returnValue;
		return new Point((int) Math.round((Double) pos[0]),
				(int) Math.round((Double) pos[1]));
	};

	/**
	 * {@link IConverter} that converts objects to {@link Rectangle}s. Returns a
	 * {@link Point} if the object is an array of two {@link Double}s. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, Rectangle> CONVERTER_RECTANGLE = returnValue -> {
		if (returnValue == null || !Object[].class.isInstance(returnValue)
				|| ((Object[]) returnValue).length != 4
				|| !Double.class.isInstance(((Object[]) returnValue)[0])
				|| !Double.class.isInstance(((Object[]) returnValue)[1])
				|| !Double.class.isInstance(((Object[]) returnValue)[2])
				|| !Double.class.isInstance(((Object[]) returnValue)[3])) {
			return null;
		}
		Object[] pos = (Object[]) returnValue;
		return new Rectangle((int) Math.round((Double) pos[0]),
				(int) Math.round((Double) pos[1]),
				(int) Math.round((Double) pos[2]),
				(int) Math.round((Double) pos[3]));
	};

	/**
	 * {@link IConverter} that converts objects to {@link Double}s. Returns a
	 * {@link Double} if the object is of type {@link Double}. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, Double> CONVERTER_DOUBLE = returnValue -> {
		if (returnValue == null || !Double.class.isInstance(returnValue)) {
			return null;
		}
		return (Double) returnValue;
	};

	public static final IConverter<Object, List<Double>> CONVERTER_DOUBLELIST = returnValue -> {
		if (returnValue == null) {
			return null;
		} else if (Object[].class.isInstance(returnValue)) {
			List<Double> doubles = new LinkedList<Double>();
			for (int i = 0, m = ((Object[]) returnValue).length; i < m; i++) {
				Object obj = ((Object[]) returnValue)[i];
				Double d = CONVERTER_DOUBLE.convert(obj);
				if (d != null) {
					doubles.add(d);
				}
			}
			return doubles;
		} else {
			return Arrays.asList(CONVERTER_DOUBLE.convert(returnValue));
		}
	};

	public static final IConverter<Object, List<Integer>> CONVERTER_INTEGERLIST = returnValue -> {
		if (returnValue == null) {
			return null;
		} else {
			return CONVERTER_DOUBLELIST.convert(returnValue).stream()
					.map(d -> (int) Math.round(d)).collect(Collectors.toList());
		}
	};

	public DEST convert(SRC returnValue);
}