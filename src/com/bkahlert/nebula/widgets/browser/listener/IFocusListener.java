package com.bkahlert.nebula.widgets.browser.listener;

import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

public interface IFocusListener {
	public void focusGained(IElement element);

	public void focusLost(IElement element);
}