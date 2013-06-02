package com.bkahlert.nebula.screenshots;

import java.util.concurrent.Callable;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Implementors can render {@link IScreenshotRequest}s.
 * 
 * @author bkahlert
 * 
 */
public interface IScreenshotRenderer<ORDER extends IScreenshotRequest> {

	/**
	 * Implementors encapsulates all information concerning a
	 * {@link IScreenshotRenderer} session.
	 * 
	 * @author bkahlert
	 * 
	 */
	public interface IScreenshotRendererSession {
		/**
		 * Brings the renderer to the front.
		 */
		public void bringToFront();

		/**
		 * Returns the {@link Rectangle} that describes the area that shows the
		 * rendered content.
		 * 
		 * @return
		 */
		public Rectangle getBounds();

		/**
		 * Disposes this {@link IScreenshotRendererSession}. You have to call
		 * this method in order to release all occupied resources.
		 */
		public void dispose();
	}

	/**
	 * Returns a {@link Callable} that renders the given
	 * {@link IScreenshotRequest} on request.
	 * 
	 * @param order
	 * @return
	 */
	public Callable<IScreenshotRendererSession> render(ORDER order);

	/**
	 * Disposes all resources this {@link IScreenshotRenderer} occupies.
	 */
	public void dispose();
}
