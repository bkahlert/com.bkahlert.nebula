package com.bkahlert.nebula.screenshots.webpage;

import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.widgets.browser.extended.IJQueryBrowser;

public interface IWebpageScreenshotRenderer<WEBPAGE extends IWebpage, BROWSER extends IJQueryBrowser>
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
