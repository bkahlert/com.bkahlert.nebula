package com.bkahlert.devel.nebula.utils;

import org.eclipse.swt.graphics.Point;

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
	public static final IConverter<Object, Void> CONVERTER_VOID = new IConverter<Object, Void>() {
		@Override
		public Void convert(Object returnValue) {
			return null;
		}
	};

	/**
	 * {@link IConverter} that converts objects to {@link Boolean}s. Converts to
	 * <code>true</code> if the given object is of type {@link Boolean} and of
	 * value <code>true</code>. Otherwise converts to <code>false</code>.
	 */
	public static final IConverter<Object, Boolean> CONVERTER_BOOLEAN = new IConverter<Object, Boolean>() {
		@Override
		public Boolean convert(Object returnValue) {
			if (returnValue == null || !Boolean.class.isInstance(returnValue)) {
				return false;
			}
			return (Boolean) returnValue;
		}
	};

	/**
	 * {@link IConverter} that converts objects to {@link Strings}s. Returns a
	 * {@link String} if the object is of type {@link String}. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, String> CONVERTER_STRING = new IConverter<Object, String>() {
		@Override
		public String convert(Object returnValue) {
			if (returnValue == null || !String.class.isInstance(returnValue)) {
				return null;
			}
			return (String) returnValue;
		}
	};

	/**
	 * {@link IConverter} that converts objects to {@link Point}s. Returns a
	 * {@link Point} if the object is an array of two {@link Double}s. Otherwise
	 * converts to <code>null</code>.
	 */
	public static final IConverter<Object, Point> CONVERTER_POINT = new IConverter<Object, Point>() {
		@Override
		public Point convert(Object returnValue) {
			if (returnValue == null || !Object[].class.isInstance(returnValue)
					|| ((Object[]) returnValue).length != 2
					|| !Double.class.isInstance(((Object[]) returnValue)[0])
					|| !Double.class.isInstance(((Object[]) returnValue)[1])) {
				return null;
			}
			Object[] pos = (Object[]) returnValue;
			return new Point((int) Math.round((Double) pos[0]),
					(int) Math.round((Double) pos[1]));
		}
	};

	public DEST convert(SRC returnValue);
}