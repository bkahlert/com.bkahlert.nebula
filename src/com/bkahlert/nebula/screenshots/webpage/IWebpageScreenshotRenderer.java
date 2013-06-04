package com.bkahlert.nebula.screenshots.webpage;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer;

public interface IWebpageScreenshotRenderer<REQUEST extends IWebpageScreenshotRequest>
		extends IScreenshotRenderer<REQUEST> {

	/**
	 * Returns the {@link IJQueryEnabledBrowserComposite} used to render the
	 * given request.
	 * 
	 * @param request
	 * @return null if no renderer is ready yet.
	 */
	public IJQueryEnabledBrowserComposite getBrowser(REQUEST request);

}
