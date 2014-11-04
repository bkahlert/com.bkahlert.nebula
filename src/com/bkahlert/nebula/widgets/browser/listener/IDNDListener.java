package com.bkahlert.nebula.widgets.browser.listener;

import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

public interface IDNDListener {
	public void dragStart(long offsetX, long offsetY, IElement element,
			String mimeType, String data);

	public void drop(long offsetX, long offsetY, IElement element,
			String mimeType, String data);
}
