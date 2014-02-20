package com.bkahlert.devel.nebula.widgets.browser.listener;

import com.bkahlert.devel.nebula.widgets.browser.extended.html.IElement;

public interface IFocusListener {
	public void focusGained(IElement element);

	public void focusLost(IElement element);
}