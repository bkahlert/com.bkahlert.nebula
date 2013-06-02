package com.bkahlert.nebula.screenshots.impl.webpage;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.screenshots.webpage.IWebpageFormFiller;

public class SearchFormFiller implements IWebpageFormFiller {
	private final String query;

	public SearchFormFiller(String query) {
		this.query = query;
	}

	@Override
	public void fill(final IJQueryEnabledBrowserComposite browser) {
		// TODO establish old setting
		browser.setAllowLocationChange(true);

		ISelector selector = new ISelector.NameSelector("q");
		browser.val(selector, query);
		browser.submit(selector);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}