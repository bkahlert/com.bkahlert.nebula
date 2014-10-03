package com.bkahlert.nebula.widgets.browser.listener;

public interface IDNDListener {
	public void dragStart(long offsetX, long offsetY, String mimeType,
			String data);

	public void drop(long offsetX, long offsetY, String mimeType, String data);
}
