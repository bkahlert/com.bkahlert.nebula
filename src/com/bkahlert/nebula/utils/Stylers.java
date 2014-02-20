package com.bkahlert.nebula.utils;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.colors.ColorUtils;

public class Stylers {

	private static final Color DEFAULT_COLOR = Display.getDefault()
			.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
	private static final Color COUNTER_COLOR = new Color(Display.getCurrent(),
			new RGB(0, 128, 170));
	private static final Color MINOR_COLOR = new Color(Display.getCurrent(),
			new RGB(179, 179, 179));

	public static final StyledString.Styler DEFAULT_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SYSTEM_FONT;
			textStyle.foreground = DEFAULT_COLOR;
		}
	};

	public static final StyledString.Styler BOLD_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.BOLD_FONT;
		}
	};

	public static final StyledString.Styler COUNTER_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
			textStyle.foreground = COUNTER_COLOR;
		}
	};

	public static final StyledString.Styler MINOR_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
			textStyle.foreground = MINOR_COLOR;
		}
	};

	public static final StyledString.Styler ATTENTION_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
			textStyle.foreground = ColorUtils.createNiceColor(0.014);
		}
	};

	private Stylers() {
	}
}
