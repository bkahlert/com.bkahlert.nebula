package com.bkahlert.devel.nebula.widgets.editor;

public interface IAnkerListener {
	/**
	 * This method is called if an {@link IAnker} was clicked.
	 * <p>
	 * <strong>Warning:</strong> Only {@link IAnker#getHref()} returns a value.
	 * The other methods return null; FIXME: Fill other values
	 * 
	 * @param anker
	 */
	public void ankerClicked(IAnker anker);
}
