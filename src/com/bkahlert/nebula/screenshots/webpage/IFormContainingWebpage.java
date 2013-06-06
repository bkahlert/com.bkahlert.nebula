package com.bkahlert.nebula.screenshots.webpage;

import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;

public interface IFormContainingWebpage extends IWebpage {

	public static interface IFieldFill {
		public ISelector getFieldSelector();

		public String getFieldValue();
	}

	public static enum Strategy {
		FILL_FIRST, FILL_ALL;
	}

	public Iterable<IFieldFill> getFieldFills();

	public Strategy getStrategy();

	public int getWait();
}
