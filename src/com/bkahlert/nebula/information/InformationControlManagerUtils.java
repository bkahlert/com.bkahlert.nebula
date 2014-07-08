package com.bkahlert.nebula.information;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.bkahlert.nebula.SourceProvider;

public class InformationControlManagerUtils {

	public static IEvaluationContext getEvaluationContext() {
		IEvaluationService service = (IEvaluationService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getService(IEvaluationService.class);
		IEvaluationContext evaluationContext = service.getCurrentState();
		return evaluationContext;
	}

	@SuppressWarnings("unchecked")
	public static InformationControlManager<Control, Object> getCurrentManager() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object manager = evaluationContext.getVariable(SourceProvider.MANAGER);
		if (manager instanceof InformationControlManager) {
			return (InformationControlManager<Control, Object>) manager;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <INFORMATION> InformationControlManager<Control, INFORMATION> getCurrentManager(
			Class<INFORMATION> informationClass) {
		Assert.isLegal(informationClass != null);

		InformationControlManager<Control, INFORMATION> manager = (InformationControlManager<Control, INFORMATION>) getCurrentManager();
		if (manager != null
				&& manager.getInformationClass().isAssignableFrom(
						informationClass)) {
			return manager;
		}
		return null;
	}

	public static InformationControl<?> getCurrentControl() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object control = evaluationContext.getVariable(SourceProvider.CONTROL);
		if (control instanceof InformationControl) {
			return (InformationControl<?>) control;
		} else {
			return null;
		}
	}

	public static Object getCurrentInput() {
		IEvaluationContext evaluationContext = getEvaluationContext();
		if (evaluationContext == null) {
			return null;
		}
		Object input = evaluationContext.getVariable(SourceProvider.INPUT);
		if (input != SourceProvider.NULL_INPUT) {
			return input;
		} else {
			return null;
		}
	}

	/**
	 * Crops the given bounds such that they lie completely on the closest
	 * monitor.
	 * 
	 * @param bounds
	 *            shell bounds to crop
	 * @since 3.4
	 */
	public static void cropToClosestMonitor(Display display, Rectangle bounds) {
		Rectangle monitorBounds = getClosestMonitor(display, bounds)
				.getClientArea();
		bounds.intersect(monitorBounds);
	}

	/**
	 * Copied from org.eclipse.jface.window.Window. Returns the monitor whose
	 * client area contains the given point. If no monitor contains the point,
	 * returns the monitor that is closest to the point. If this is ever made
	 * public, it should be moved into a separate utility class.
	 * 
	 * @param display
	 *            the display to search for monitors
	 * @param rectangle
	 *            the rectangle to find the closest monitor for (display
	 *            coordinates)
	 * @return the monitor closest to the given point
	 * @since 3.3
	 */
	public static Monitor getClosestMonitor(Display display, Rectangle rectangle) {
		int closest = Integer.MAX_VALUE;

		Point toFind = Geometry.centerPoint(rectangle);
		Monitor[] monitors = display.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(
					Geometry.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}
}
