package com.bkahlert.nebula.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;

/**
 * @author Björn Kahlert
 * @author 
 *         http://www.koders.com/java/fidA31ED1D8FE04A401EB6D7A8D1B8642AB2969077D
 *         .aspx?s=Alignment
 * 
 */
public class ColorPicker extends CLabel {

	public static interface IModificationListener {
		public void colorChanged(RGB rgb);
	}

	private final List<IModificationListener> modificationListeners = new ArrayList<IModificationListener>();

	private Color color;
	private RGB rgb;

	public ColorPicker(Composite parent, final RGB rgb) {
		super(parent, SWT.SHADOW_OUT);
		this.update(rgb);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ColorDialog dialog = new ColorDialog(new Shell(Display
						.getDefault(), SWT.SHELL_TRIM));
				dialog.setRGB(ColorPicker.this.color.getRGB());
				RGB selected = new RGB(dialog.open());
				if (selected != null) {
					ColorPicker.this.update(selected);
				}
			}
		});
	}

	public ColorPicker(Composite parent) {
		this(parent, null);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = super.computeSize(wHint, hHint, changed);
		if (size.x < 40) {
			size.x = 40;
		}
		return size;
	}

	@Override
	public Rectangle getClientArea() {
		Rectangle clientArea = super.getClientArea();
		clientArea.width = 500;
		return clientArea;
	}

	private void update(RGB rgb) {
		if (this.color != null && !this.color.isDisposed()) {
			this.color.dispose();
		}

		if (rgb == null) {
			rgb = ColorUtils.getRandomRGB();
		}
		this.color = new Color(Display.getDefault(), rgb.toClassicRGB());
		this.rgb = rgb;
		this.setBackground(this.color);
		this.notifyChanged(rgb);
	}

	public void addModificationListener(
			IModificationListener modificationListener) {
		this.modificationListeners.add(modificationListener);
	}

	public void removeModificationListener(
			IModificationListener modificationListener) {
		this.modificationListeners.remove(modificationListener);
	}

	private void notifyChanged(RGB rgb) {
		for (IModificationListener modificationListener : this.modificationListeners) {
			modificationListener.colorChanged(rgb);
		}
	}

	/**
	 * @return the Color most recently selected by the user.
	 *         <em>Note that it is the responsibility of the client to
	 *         dispose this resource</em>
	 */
	public RGB getRGB() {
		return this.rgb;
	}

	public void setRGB(RGB rgb) {
		this.update(rgb);
	}

	@Override
	public void dispose() {
		if (this.color != null && !this.color.isDisposed()) {
			this.color.dispose();
		}
		super.dispose();
	}
}