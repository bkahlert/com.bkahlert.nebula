package com.bkahlert.devel.nebula.utils.history;

import com.bkahlert.devel.nebula.utils.history.History.NoNextElementException;
import com.bkahlert.devel.nebula.utils.history.History.NoPreviousElementException;

/**
 * This class provides the functionality needed to implement a browser like
 * history.
 * 
 * @author bkahlert
 * 
 * @param <T>
 *            type this {@link IHistory} can hold
 */
public interface IHistory<T> {

	/**
	 * Returns the current position in this {@link History}.
	 * 
	 * @return -1 if empty
	 */
	public int pos();

	/**
	 * Returns the number of elements in this {@link History}.
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Returns true if you can navigate one step backwards.
	 * 
	 * @return
	 */
	public boolean hasPrev();

	/**
	 * Returns true if you can navigate one step forward.
	 * 
	 * @return
	 */
	public boolean hasNext();

	/**
	 * Returns the currently active element in this {@link History}.
	 * 
	 * @return null if empty
	 */
	public T get();

	/**
	 * Adds one element to this {@link History} and makes it the currently
	 * active element.
	 * 
	 * @param element
	 */
	public void add(T element);

	/**
	 * Moves one element back in this {@link History} and returns the newly
	 * active element.
	 * 
	 * @return
	 * @throws NoPreviousElementException
	 */
	public T back() throws NoPreviousElementException;

	/**
	 * Moves one element forward in this {@link History} and returns the newly
	 * active element.
	 * 
	 * @return
	 * @throws NoNextElementException
	 */
	public T forward() throws NoNextElementException;

	/**
	 * Empties this {@link IHistory}.
	 */
	public void clear();

}