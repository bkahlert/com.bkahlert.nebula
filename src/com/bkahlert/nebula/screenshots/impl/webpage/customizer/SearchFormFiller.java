package com.bkahlert.nebula.screenshots.impl.webpage.customizer;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;

public class SearchFormFiller extends WebpageScreenshotCustomizer {
	private final String query;

	public SearchFormFiller(String query) {
		this.query = query;
	}

	@Override
	public void betweenLoadingAndScrolling(IWebpageScreenshotRequest request,
			IJQueryEnabledBrowserComposite browserComposite) {
		// TODO establish old setting
		browserComposite.setAllowLocationChange(true);

		ISelector selector = new ISelector.NameSelector("q");
		browserComposite.val(selector, this.query);
		browserComposite.submit(selector);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}