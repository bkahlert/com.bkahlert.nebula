package com.bkahlert.nebula.screenshots;

import java.util.concurrent.Callable;

import org.eclipse.swt.widgets.Control;

/**
 * Implementors can render {@link IGenericScreenshotRequest}s.
 * 
 * @author bkahlert
 * 
 */
public interface IScreenshotRenderer<SUBJECT, CONTROL> {

	/**
	 * Implementors encapsulates all information concerning a
	 * {@link IScreenshotRenderer} session.
	 * 
	 * @author bkahlert
	 * 
	 */
	public interface IScreenshotRendererSession {
		/**
		 * Makes sure the content is currently displayed in the returned
		 * Control.
		 */
		public Control display();

		/**
		 * Disposes this {@link IScreenshotRendererSession}. You have to call
		 * this method in order to release all occupied resources.
		 */
		public void dispose();
	}

	/**
	 * Returns a {@link Callable} that renders the given
	 * {@link IGenericScreenshotRequest} on request.
	 * 
	 * @param request
	 * @return
	 */
	public Callable<IScreenshotRendererSession> render(SUBJECT subject);

	/**
	 * This method is called when the subject is fully rendered and displayed in
	 * the given control.
	 * 
	 * @param subject
	 * @param control
	 */
	public void renderingFinished(SUBJECT subject, CONTROL control);

	/**
	 * Disposes all resources this {@link IScreenshotRenderer} occupies.
	 */
	public void dispose();
}
