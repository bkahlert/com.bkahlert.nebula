package com.bkahlert.nebula.utils;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;

public interface IModifiable {

	/**
	 * Adds a {@link ModifyListener} to this {@link Control}.
	 * <p>
	 * Please note that {@link ModifyListener#modifyText(ModifyEvent)} is also
	 * fired when the {@link Control} is being disposed.
	 * 
	 * @param modifyListener
	 */
	public abstract void addModifyListener(ModifyListener modifyListener);

	/**
	 * Removes a {@link ModifyListener} from this {@link Control}.
	 * 
	 * @param modifyListener
	 */
	public abstract void removeModifyListener(ModifyListener modifyListener);

}