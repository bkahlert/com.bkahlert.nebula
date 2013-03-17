package com.bkahlert.devel.nebula.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This class helps by delegating the registration of event listeners.
 * <p>
 * This can be useful for {@link Composite}s who's child controls catch the
 * events making the {@link Composite} not notice any events.
 * 
 * @author bkahlert
 * 
 */
public class EventDelegator {
	/**
	 * Returns true if the given eventType can't be processed by the given
	 * {@link Control}.
	 * 
	 * @param eventType
	 * @param clazz
	 * @return
	 */
	public static boolean mustDelegate(int eventType, Control control) {
		Assert.isLegal(control != null);
		if (control instanceof Composite) {
			return eventType == SWT.MouseMove || eventType == SWT.MouseEnter
					|| eventType == SWT.MouseHover
					|| eventType == SWT.MouseExit;
		}
		return false;
	}
}
