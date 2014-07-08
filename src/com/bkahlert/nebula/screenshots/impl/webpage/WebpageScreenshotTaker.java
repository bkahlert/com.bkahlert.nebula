package com.bkahlert.nebula.screenshots.impl.webpage;

import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.impl.ScreenshotTaker;
import com.bkahlert.nebula.screenshots.webpage.IWebpage;

public class WebpageScreenshotTaker<WEBPAGE extends IWebpage> extends
		ScreenshotTaker<WEBPAGE> {

	public WebpageScreenshotTaker(int numThreads,
			IScreenshotRenderer<WEBPAGE, ?> renderer) {
		super(numThreads, renderer);
	}

}
