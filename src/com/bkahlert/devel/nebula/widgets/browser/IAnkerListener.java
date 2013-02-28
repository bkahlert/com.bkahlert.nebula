package com.bkahlert.devel.nebula.widgets.browser;

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

	/**
	 * This method is called if an {@link IAnker} was clicked in a special way
	 * (typically while holding ⌃ (Ctrl) or ⌘ (Command)).
	 * 
	 * @param anker
	 */
	public void ankerClickedSpecial(IAnker anker);
}
