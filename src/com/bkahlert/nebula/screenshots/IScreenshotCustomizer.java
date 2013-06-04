package com.bkahlert.nebula.screenshots;

/**
 * Instances of this class can influence the screenshot taking process.
 * 
 * @author bkahlert
 * 
 * @param <REQUEST>
 */
public interface IScreenshotCustomizer {
	public void betweenLoadingAndScrolling(IScreenshotRequest request,
			IScreenshotRenderer<? extends IScreenshotRequest> renderer);
}