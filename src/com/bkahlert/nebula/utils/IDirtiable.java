package com.bkahlert.nebula.utils;

public interface IDirtiable {

	/**
	 * Updates the time stamp returned by {@link #getLastModification()}.
	 */
	public void modified();

	/**
	 * Returns the time stamp of the last call of {@link #modified()}.
	 *
	 * @return
	 */
	public long getLastModification();

}