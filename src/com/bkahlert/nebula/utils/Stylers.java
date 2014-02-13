package com.bkahlert.nebula.utils;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.utils.FontUtils;

public interface Stylers {
	static final LocalResourceManager RESOURCES = new LocalResourceManager(
			JFaceResources.getResources());

	static final Font SYSTEM_FONT = Display.getDefault().getSystemFont();

	static final Font BOLD_FONT = RESOURCES.createFont(FontDescriptor
			.createFrom(FontUtils.getModifiedFontData(
					SYSTEM_FONT.getFontData(), SWT.BOLD)));

	static final double SMALL_TEXT_FACTOR = .8;

	static final Font SMALL_FONT = RESOURCES.createFont(FontDescriptor
			.createFrom(FontUtils.getResizedFontData(SYSTEM_FONT.getFontData(),
					SMALL_TEXT_FACTOR)));

	static final int SMALL_FONT_RISE = (int) ((SYSTEM_FONT.getFontData()[0]
			.getHeight() - SMALL_FONT.getFontData()[0].getHeight()) / 2.0);

	static final Color COUNTER_COLOR = new Color(Display.getCurrent(), new RGB(
			0, 128, 170));
	static final Color MINOR_COLOR = new Color(Display.getCurrent(), new RGB(
			179, 179, 179));

	public static final StyledString.Styler BOLD_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = BOLD_FONT;
		}
	};

	public static final StyledString.Styler COUNTER_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = SMALL_FONT;
			textStyle.rise = SMALL_FONT_RISE;
			textStyle.foreground = COUNTER_COLOR;
		}
	};

	public static final StyledString.Styler MINOR_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = SMALL_FONT;
			textStyle.rise = SMALL_FONT_RISE;
			textStyle.foreground = MINOR_COLOR;
		}
	};

	public static final StyledString.Styler ATTENTION_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = SMALL_FONT;
			textStyle.rise = SMALL_FONT_RISE;
			textStyle.foreground = ColorUtils.createNiceColor(0.014);
		}
	};
}
