package com.bkahlert.nebula.screenshots.impl.webpage;

import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.screenshots.impl.ScreenshotTaker;
import com.bkahlert.nebula.screenshots.webpage.IWebpageScreenshotRequest;

public class WebpageScreenshotTaker extends
		ScreenshotTaker<IWebpageScreenshotRequest> {

	public WebpageScreenshotTaker(int numThreads, Shell parentShell) {
		super(numThreads, new WebpageRenderer(parentShell));
	}

}
