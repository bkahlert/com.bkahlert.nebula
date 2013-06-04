package com.bkahlert.nebula.screenshots.impl.webpage.customizer;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.nebula.screenshots.IScreenshotCustomizer;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRenderer;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;

public abstract class WebpageScreenshotCustomizer implements
		IScreenshotCustomizer {

	@Override
	public final void betweenLoadingAndScrolling(IScreenshotRenderer renderer) {
		if (renderer instanceof IWebpageScreenshotRenderer) {
			IJQueryEnabledBrowserComposite browserComposite = ((IWebpageScreenshotRenderer<IWebpageScreenshotRequest>) renderer)
					.getBrowser(request);
			this.betweenLoadingAndScrolling(request, browserComposite);
		}
	}

	public abstract void betweenLoadingAndScrolling(
			IWebpageScreenshotRequest request,
			IJQueryEnabledBrowserComposite browserComposite);

}
