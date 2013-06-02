package com.bkahlert.nebula.screenshots;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Instances of this class provide the functionality to make complex screenshots
 * that is screenshot who need preparation such a opening a webpage.
 * 
 * @author bkahlert
 * 
 * @param <REQUEST>
 */
public interface IScreenshotTaker<REQUEST extends IScreenshotRequest> {

	/**
	 * Submit the given {@link IScreenshotRequest} and immediately returns a
	 * {@link Future} containing the location of the generated screenshot.
	 * <p>
	 * <strong>WARNING!<br>
	 * The {@link Future#get()} must not be called from the UI thread without
	 * checking {@link Future#isDone()}. Because the UI thread itself is needed
	 * for the {@link Future} to finish its computation, calling
	 * {@link Future#get()} before it is done results in a deadlock!</strong>.
	 * 
	 * @param order
	 * @return
	 */
	public Future<File> submitOrder(REQUEST order);

	/**
	 * Disposes all resources this {@link IScreenshotTaker} occupies.
	 */
	public void dispose();

}
