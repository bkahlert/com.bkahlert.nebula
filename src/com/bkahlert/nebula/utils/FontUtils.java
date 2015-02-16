package com.bkahlert.nebula.utils;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FontUtils {

	private static final Logger LOGGER = Logger.getLogger(FontUtils.class);

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
			.createFont(FontDescriptor.createFrom(addStyle(
					SYSTEM_FONT.getFontData(), SWT.BOLD)));

	/**
	 * The system's default font in italic.
	 */
	public static final Font ITALIC_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(addStyle(
					SYSTEM_FONT.getFontData(), SWT.ITALIC)));

	/**
	 * Factor by which the small font is actually smaller than the system's
	 * default font.
	 */
	public static final double SMALL_TEXT_FACTOR = .9;

	/**
	 * Factor by which the large font is actually larger than the system's
	 * default font.
	 */
	public static final double LARGE_TEXT_FACTOR = 1.15;

	/**
	 * A smaller font compared to the system's default font.
	 */
	public static final Font SMALL_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(resize(
					SYSTEM_FONT.getFontData(), SMALL_TEXT_FACTOR)));

	/**
	 * A larger font compared to the system's default font.
	 */
	public static final Font LARGE_FONT = FontUtils.RESOURCES
			.createFont(FontDescriptor.createFrom(resize(
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
		control.addDisposeListener(e -> newFont.dispose());
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
		control.addDisposeListener(e -> newFont.dispose());
	}

	/**
	 * Adds the given style bitmask to the one's of the given {@link FontData}.
	 *
	 * @param originalData
	 * @param additionalStyle
	 * @return
	 */
	public static FontData[] addStyle(FontData[] originalData,
			int additionalStyle) {
		FontData[] styledData = new FontData[originalData.length];
		for (int i = 0; i < originalData.length; i++) {
			styledData[i] = new FontData(originalData[i].getName(),
					originalData[i].getHeight(), originalData[i].getStyle()
							| additionalStyle);
		}
		return styledData;
	}

	/**
	 * Adds the given style bitmask to the onf of the given {@link Font}.
	 * <p>
	 * <strong>Attention:</strong> A new object is created that must be manually
	 * disposed.
	 *
	 * @param originalFont
	 * @param additionalStyle
	 * @return
	 */
	public static Font addStyle(Font originalFont, int additionalStyle) {
		FontData[] modified = addStyle(originalFont.getFontData(),
				additionalStyle);
		return new Font(Display.getDefault(), modified);
	}

	/**
	 * Returns new {@link FontData} with the scaled size based on the given
	 * {@link FontData} and the given scale factor.
	 *
	 * @param originalData
	 * @param resizeBy
	 * @return
	 */
	public static FontData[] resize(FontData[] originalData, double resizeBy) {
		FontData[] styledData = new FontData[originalData.length];
		for (int i = 0; i < originalData.length; i++) {
			styledData[i] = new FontData(originalData[i].getName(),
					(int) (originalData[i].getHeight() * resizeBy),
					originalData[i].getStyle());
		}
		return styledData;
	}

	/**
	 * Merges the given {@link FontData} to form a new set of {@link FontData}
	 * representing a mixture out of both {@link FontData}.
	 * <p>
	 * Merging rules:
	 * <dl>
	 * <dt>Name</dt>
	 * <dd>... originates from the second font.</dd>
	 * <dt>Locale</dt>
	 * <dd>... originates from the second font.</dd>
	 * <dt>Size</dt>
	 * <dd>... is the product of the fonts's derivations from the system font
	 * size.<br/>
	 * Examples:
	 * <ul>
	 * <li>normal + small = small</li>
	 * <li>large + small = normal</li>
	 * <li>small + small = even smaller</li>
	 * </ul>
	 * </dd>
	 * <dt>Style</dt>
	 * <dd>... uses all defined flags. As soon as at least one font is bold or
	 * italic (or both) the resulting font has the same style.</dd>
	 * </dl>
	 *
	 * @param font1
	 * @param font2
	 * @return
	 */
	public static FontData[] merge(FontData[] font1, FontData[] font2) {
		LOGGER.info("Merging fonts:\n\t\t" + font1[0] + "\n\t\t" + font2[0]);
		if (font1 == null || font1.length == 0) {
			LOGGER.info("\tUsing: " + font2[0]);
			return font2;
		}
		if (font2 == null || font2.length == 0) {
			LOGGER.info("\ttUsing: " + font1[0]);
			return font1;
		}
		FontData fontData[] = new FontData[font1.length];
		for (int i = 0; i < font1.length; i++) {
			FontData fontData1 = font1[i];
			FontData fontData2 = font2[i];
			FontData merged = new FontData();

			merged.setName(fontData2.getName());
			merged.setLocale(fontData2.getLocale());
			LOGGER.debug("\tSizes: font1(" + fontData1.getHeight() + ") font2("
					+ fontData2.getHeight() + ") sys("
					+ FontUtils.SYSTEM_FONT.getFontData()[0].getHeight() + ")");
			merged.setHeight((int) Math.round(((double) fontData1.getHeight() / (double) FontUtils.SYSTEM_FONT
					.getFontData()[0].getHeight()) * fontData2.getHeight()));
			if (SMALL_FONT.getFontData()[0].getHeight() == fontData1
					.getHeight()
					&& fontData1.getHeight() == fontData2.getHeight()) {
			}
			merged.setStyle(fontData1.getStyle() | fontData2.getStyle());
			fontData[i] = merged;
		}
		LOGGER.info("\tMerged: " + fontData[0]);
		return fontData;
	}

	private static Shell shell = null;
	private static Label label = null;

	/**
	 * Calculates the space needed to render the given text.
	 *
	 * @param text
	 * @return
	 */
	public static Future<Point> calcSize(final String text) {
		return ExecUtils.asyncExec(() -> {
			if (shell == null) {
				shell = new Shell(Display.getCurrent());
				shell.setLayout(new RowLayout());
			}
			if (label == null) {
				label = new Label(shell, SWT.NONE);
			}
			GC gc = new GC(label);
			Point size = gc.textExtent(text);
			gc.dispose();
			return size;
		});
	}

}
