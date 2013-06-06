package com.bkahlert.nebula.screenshots.webpage;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer;

public interface IWebpageScreenshotRenderer<WEBPAGE extends IWebpage, BROWSER extends IJQueryEnabledBrowserComposite>
		extends IScreenshotRenderer<WEBPAGE, BROWSER> {

	/**
	 * This method is called when the browser is ready to load the web page.
	 * This means the browser is opened to correctly resized.
	 * 
	 * @param webpage
	 * @param browser
	 */
	public void preparedWebpageControlFinished(WEBPAGE webpage, BROWSER browser);

	/**
	 * This method is called when the browser finished loading the web page.
	 * 
	 * @param webpage
	 * @param browser
	 */
	public void loadingWebpageFinished(WEBPAGE webpage, BROWSER browser);

	/**
	 * This method is called when the browser scrolled to the defined position
	 * within the loaded web page.
	 * 
	 * @param webpage
	 * @param browser
	 */
	public void scrollingWebpageFinished(WEBPAGE webpage, BROWSER browser);

}
