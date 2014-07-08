package com.bkahlert.nebula.widgets;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

public interface IWidget {
	/**
	 * @see Composite#layout()
	 */
	public void layout();

	/**
	 * @see Widget#getData()
	 */
	public Object getData();

	/**
	 * @see Widget#getData(String)
	 */
	public Object getData(String key);

	/**
	 * @see Widget#setData(Object)
	 */
	public void setData(Object data);

	/**
	 * @see Widget#setData(String, Object)
	 */
	public void setData(String key, Object value);

	/**
	 * @see Widget#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener disposeListener);

	/**
	 * @see Widget#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener disposeListener);

	/**
	 * @see Widget#isDisposed()
	 */
	public boolean isDisposed();

	/**
	 * @see Widget#dispose()
	 */
	public void dispose();
}
