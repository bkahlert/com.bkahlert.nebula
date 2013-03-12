package com.bkahlert.devel.nebula.utils.history;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a base implementation if the {@link IHistory}.
 * 
 * @author bkahlert
 * 
 * @param <T>
 *            type this {@link History} can hold
 */
public class History<T> implements IHistory<T> {

	public static class NoPreviousElementException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public static class NoNextElementException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private List<T> elements;
	private int pos;

	public History() {
		this.elements = new ArrayList<T>();
		this.pos = -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#pos()
	 */
	@Override
	public int pos() {
		return this.pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#size()
	 */
	@Override
	public int size() {
		return this.elements.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#hasPrev()
	 */
	@Override
	public boolean hasPrev() {
		return this.pos - 1 >= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.pos + 1 <= this.elements.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#get()
	 */
	@Override
	public T get() {
		if (this.pos == -1) {
			return null;
		}
		return this.elements.get(this.pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#add(T)
	 */
	@Override
	public void add(T element) {
		while (this.pos != this.elements.size() - 1) {
			this.elements.remove(this.elements.size() - 1);
		}
		this.elements.add(element);
		this.pos = this.elements.size() - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#back()
	 */
	@Override
	public T back() throws NoPreviousElementException {
		if (!this.hasPrev()) {
			throw new NoPreviousElementException();
		}
		this.pos--;
		return this.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bkahlert.devel.nebula.utils.history.IHistory#forward()
	 */
	@Override
	public T forward() throws NoNextElementException {
		if (!this.hasNext()) {
			throw new NoNextElementException();
		}
		this.pos++;
		return this.get();
	}

	@Override
	public void clear() {
		this.elements.clear();
		this.pos = -1;
	}

}
