package com.bkahlert.nebula.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

import com.bkahlert.nebula.utils.colors.ColorUtils;

public class SWTUtils {

	public static Point getMainScreenSize() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return new Point(width, height);
	}

	public static Point getDisplaySize() {
		try {
			return ExecUtils.syncExec(new Callable<Point>() {
				@Override
				public Point call() throws Exception {
					Rectangle bounds = Display.getCurrent().getBounds();
					return new Point(bounds.width, bounds.height);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Disposes all child {@link Control}s of the given {@link Composite}.
	 *
	 * @param control
	 */
	public static void clearControl(Composite composite) {
		for (Control control : composite.getChildren()) {
			if (!control.isDisposed()) {
				control.dispose();
			}
		}
	}

	/**
	 * Runs up the {@link Composite} hierarchy and returns the first occurrence
	 * of the given type.
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Composite> T getParent(Class<T> clazz,
			Control control) {
		if (control == null) {
			return null;
		}
		Composite parent = control.getParent();
		if (parent == null) {
			return null;
		}
		if (clazz.isInstance(parent)) {
			return (T) parent;
		} else {
			return getParent(clazz, parent);
		}
	}

	/**
	 * Returns the root {@link Composite} of the given {@link Control}, that is
	 * running up the parent hierarchy to the top.
	 *
	 * @param clazz
	 * @return
	 */
	public static Composite getRoot(Control control) {
		if (control == null) {
			return null;
		}
		Composite parent = control.getParent();
		if (parent == null) {
			return control instanceof Composite ? (Composite) control : null;
		} else {
			return getRoot(parent);
		}
	}

	/**
	 * Returns the number of objects of the given type in the parent hierarchy.
	 * In other words: How many Ts surround the given {@link Control}.
	 *
	 * @param control
	 * @return
	 */
	public static <T extends Composite> int getNumParents(Class<T> clazz,
			Control control) {
		int matches = 0;
		T match = null;
		while (true) {
			match = getParent(clazz, control);
			if (match != null) {
				matches++;
			} else {
				break;
			}
		}
		return matches;
	}

	private static Map<Control, Color> backgroundColor = new HashMap<Control, Color>();

	// HACK
	/**
	 * Returns the given {@link Control}s background color. This is typically
	 * the background color {@link Control#getBackground()} returns. In the case
	 * of surrounding {@link Group}s the actual background color is darker on
	 * Mac OS. This method returns the correctly darkened background color.
	 *
	 * @param control
	 * @return
	 */
	public static Color getEffectiveBackground(Control control) {
		if (control == null) {
			return null;
		}
		if (backgroundColor.containsKey(control)) {
			return backgroundColor.get(control);
		}

		Group group = getParent(Group.class, control);
		if (group != null) {
			float darkenBy = -0.033f;
			Color color = getEffectiveBackground(group.getParent());
			if (color == null) {
				color = Display.getCurrent().getSystemColor(
						SWT.COLOR_WIDGET_BACKGROUND);
			}
			color = ColorUtils.addLightness(color, darkenBy);
			backgroundColor.put(control, color);
			return color;
		} else {
			return control.getBackground();
		}
	}

	/**
	 * Returns an array that evenly distributes the elements on a
	 * {@link SashForm}.
	 *
	 * @param n
	 * @return
	 */
	public static int[] getEvenWeights(int n) {
		int[] weights = new int[n];
		int weight = 100 / n;
		for (int i = 0; i < n; i++) {
			weights[i] = weight;
		}
		return weights;
	}

}
