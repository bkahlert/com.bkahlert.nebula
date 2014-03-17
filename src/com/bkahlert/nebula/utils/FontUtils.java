package com.bkahlert.nebula.utils;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class FontUtils {

	public static final LocalResourceManager RESOURCES = new LocalResourceManager(
			JFaceResources.getResources());

	/**
	 * The system's default font.
	 */
	public static final Font SYSTEM_FONT = Display.getDefault().getSystemFont();

	/**
	 * The system's default font in bold.
	 */
	public static final Font BOLD_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(getModifiedFontData(
					SYSTEM_FONT.getFontData(), SWT.BOLD)));

	/**
	 * Factor by which the small font is actually smaller than the system's
	 * default font.
	 */
	public static final double SMALL_TEXT_FACTOR = .8;

	/**
	 * Factor by which the large font is actually larger than the system's
	 * default font.
	 */
	public static final double LARGE_TEXT_FACTOR = 1.15;

	/**
	 * A smaller font compared to the system's default font.
	 */
	public static final Font SMALL_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(getResizedFontData(
					SYSTEM_FONT.getFontData(), SMALL_TEXT_FACTOR)));

	/**
	 * A larger font compared to the system's default font.
	 */
	public static final Font LARGE_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(getResizedFontData(
					SYSTEM_FONT.getFontData(), LARGE_TEXT_FACTOR)));

	/**
	 * Number of pixels the font needs to be moved upwards in order to make it
	 * vertically middle aligned compared to the system's default font.
	 */
	public static final int SMALL_FONT_RISE = (int) ((SYSTEM_FONT.getFontData()[0]
			.getHeight() - SMALL_FONT.getFontData()[0].getHeight()) / 2.0);

	private FontUtils() {
		// no instantiation allowed
	}

	public static void changeFontSizeBy(Control control, int fontSizeInc) {
		FontData[] fontData = control.getFont().getFontData();
		for (int i = 0; i < fontData.length; ++i) {
			fontData[i].setHeight(fontData[i].getHeight() + fontSizeInc);
		}

		final Font newFont = new Font(control.getDisplay(), fontData);
		control.setFont(newFont);

		// Since you created the font, you must dispose it
		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				newFont.dispose();
			}
		});
	}

	public static void makeBold(Control control) {
		FontData[] fontData = control.getFont().getFontData();
		FontData[] styledData = new FontData[fontData.length];
		for (int i = 0; i < fontData.length; i++) {
			styledData[i] = new FontData(fontData[i].getName(),
					fontData[i].getHeight(), fontData[i].getStyle() | SWT.BOLD);
		}
		FontData[] boldFontData = styledData;

		final Font newFont = new Font(control.getDisplay(), boldFontData);
		control.setFont(newFont);

		// Since you created the font, you must dispose it
		control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				newFont.dispose();
			}
		});
	}

	public static FontData[] getModifiedFontData(FontData[] originalData,
			int additionalStyle) {
		FontData[] styledData = new FontData[originalData.length];
		for (int i = 0; i < originalData.length; i++) {
			styledData[i] = new FontData(originalData[i].getName(),
					originalData[i].getHeight(), originalData[i].getStyle()
							| additionalStyle);
		}
		return styledData;
	}

	public static FontData[] getResizedFontData(FontData[] originalData,
			double resizeBy) {
		FontData[] styledData = new FontData[originalData.length];
		for (int i = 0; i < originalData.length; i++) {
			styledData[i] = new FontData(originalData[i].getName(),
					(int) (originalData[i].getHeight() * resizeBy),
					originalData[i].getStyle());
		}
		return styledData;
	}

}
