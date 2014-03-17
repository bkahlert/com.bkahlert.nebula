package com.bkahlert.nebula.utils;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

public class Stylers {

	private static final Color DEFAULT_COLOR = Display.getDefault()
			.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
	private static final Color COUNTER_COLOR = new Color(Display.getCurrent(),
			new RGB(0, 128, 170));
	private static final Color MINOR_COLOR = new Color(Display.getCurrent(),
			new RGB(179, 179, 179));

	public static final Color ATTENTION_COLOR = new Color(Display.getDefault(),
			com.bkahlert.nebula.utils.colors.RGB.DANGER.toClassicRGB());

	public static final Color IMPORTANCE_HIGH_COLOR = new Color(
			Display.getDefault(),
			com.bkahlert.nebula.utils.colors.RGB.IMPORTANCE_HIGH.toClassicRGB());
	public static final Color IMPORTANCE_LOW_COLOR = new Color(
			Display.getDefault(),
			com.bkahlert.nebula.utils.colors.RGB.IMPORTANCE_LOW.toClassicRGB());

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

	public static final StyledString.Styler SMALL_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
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
			textStyle.foreground = MINOR_COLOR;
			textStyle.underline = true;
			textStyle.underlineColor = ATTENTION_COLOR;
			textStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
		}
	};

	public static final StyledString.Styler IMPORTANCE_HIGH_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.LARGE_FONT;
			textStyle.foreground = IMPORTANCE_HIGH_COLOR;
		}
	};

	public static final StyledString.Styler IMPORTANCE_LOW_STYLER = new StyledString.Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = IMPORTANCE_LOW_COLOR;
		}
	};

	private Stylers() {
	}
}
