package com.bkahlert.nebula.utils;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.colors.RGB;
public class Stylers {

	private static final StyleRange DEFAULT_STYLE_RANGE = new StyledString(
			"default", new Styler() {
				@Override
				public void applyStyles(TextStyle textStyle) {
				}
			}).getStyleRanges()[0];

	private static final Color COUNTER_COLOR = new Color(Display.getCurrent(),
			new org.eclipse.swt.graphics.RGB(0, 128, 170));
	private static final Color MINOR_COLOR = new Color(Display.getCurrent(),
			new org.eclipse.swt.graphics.RGB(179, 179, 179));

	public static final Color ATTENTION_COLOR = new Color(Display.getDefault(),
			RGB.DANGER.toClassicRGB());

	public static final Color IMPORTANCE_HIGH_COLOR = new Color(
			Display.getDefault(), RGB.IMPORTANCE_HIGH.toClassicRGB());
	public static final Color IMPORTANCE_LOW_COLOR = new Color(
			Display.getDefault(), RGB.IMPORTANCE_LOW.toClassicRGB());

	public static final Styler DEFAULT_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
		}
	};

	public static final Styler BOLD_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.BOLD_FONT;
		}
	};

	public static final Styler COUNTER_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
			textStyle.foreground = COUNTER_COLOR;
		}
	};

	public static final Styler SMALL_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
		}
	};

	public static final Styler MINOR_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.SMALL_FONT;
			textStyle.rise = FontUtils.SMALL_FONT_RISE;
			textStyle.foreground = MINOR_COLOR;
		}
	};

	public static final Styler ATTENTION_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.underline = true;
			textStyle.underlineColor = ATTENTION_COLOR;
			textStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
		}
	};

	public static final Styler IMPORTANCE_HIGH_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.LARGE_FONT;
			textStyle.foreground = IMPORTANCE_HIGH_COLOR;
		}
	};

	public static final Styler IMPORTANCE_LOW_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.foreground = IMPORTANCE_LOW_COLOR;
		}
	};

	public static Styler combine(final Styler... stylers) {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				for (Styler styler : stylers) {
					styler.applyStyles(textStyle);
				}
			}
		};
	}

	/**
	 * Appends the given append {@link StyledString} to the base
	 * {@link StyledString}. Format wise the baseStyler is used but is
	 * overwritten by the append {@link StyledString}'s styles.
	 * 
	 * @param string
	 * @param append
	 * @param baseStyler
	 * @return
	 */
	public static StyledString append(StyledString baseString,
			StyledString appendString, Styler baseStyler) {
		int originalLength = baseString.length();
		baseString.append(appendString.getString());
		for (StyleRange styleRange : appendString.getStyleRanges()) {
			baseString.setStyle(originalLength + styleRange.start,
					styleRange.length,
					combine(baseStyler, createFrom(styleRange)));
		}
		return baseString;
	}

	/**
	 * Appends the given append {@link StyledString} to the base
	 * {@link StyledString}. The appended string uses a merge style based on the
	 * the originals strings style and the appended one.
	 * 
	 * @param string
	 * @param append
	 * @return
	 */
	public static StyledString append(StyledString baseString,
			StyledString appendString) {
		StyleRange lastUsedStyleRange = baseString.getStyleRanges().length > 0 ? baseString
				.getStyleRanges()[baseString.getStyleRanges().length - 1]
				: null;
		Styler baseStyler = lastUsedStyleRange != null ? createFrom(lastUsedStyleRange)
				: null;
		return append(baseString, appendString, baseStyler);
	}

	public static StyledString clone(StyledString string) {
		StyledString clone = new StyledString(string.getString());
		for (StyleRange styleRange : string.getStyleRanges()) {
			clone.setStyle(styleRange.start, styleRange.length,
					createFrom(styleRange));
		}
		return clone;
	}

	/**
	 * Create a {@link Styler} that creates the style as present in the given
	 * {@link StyleRange}.
	 * <p>
	 * Only the styles that differ from the by default styles are defined. This
	 * way the resulting {@link Styler} can also be combined with other
	 * {@link Styler}s using {@link #combine(Styler...)}.
	 * 
	 * @param styleRange
	 * @return
	 */
	public static Styler createFrom(final StyleRange styleRange) {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				if (!ObjectUtils.equals(styleRange.background,
						DEFAULT_STYLE_RANGE.background)) {
					textStyle.background = styleRange.background;
				}
				if (!ObjectUtils.equals(styleRange.borderColor,
						DEFAULT_STYLE_RANGE.borderColor)) {
					textStyle.borderColor = styleRange.borderColor;
				}
				if (!ObjectUtils.equals(styleRange.borderStyle,
						DEFAULT_STYLE_RANGE.borderStyle)) {
					textStyle.borderStyle = styleRange.borderStyle;
				}
				if (!ObjectUtils.equals(styleRange.font,
						DEFAULT_STYLE_RANGE.font)) {
					textStyle.font = styleRange.font;
				}
				if (!ObjectUtils.equals(styleRange.foreground,
						DEFAULT_STYLE_RANGE.foreground)) {
					textStyle.foreground = styleRange.foreground;
				}
				if (!ObjectUtils.equals(styleRange.metrics,
						DEFAULT_STYLE_RANGE.metrics)) {
					textStyle.metrics = styleRange.metrics;
				}
				if (!ObjectUtils.equals(styleRange.rise,
						DEFAULT_STYLE_RANGE.rise)) {
					textStyle.rise = styleRange.rise;
				}
				if (!ObjectUtils.equals(styleRange.strikeout,
						DEFAULT_STYLE_RANGE.strikeout)) {
					textStyle.strikeout = styleRange.strikeout;
				}
				if (!ObjectUtils.equals(styleRange.strikeoutColor,
						DEFAULT_STYLE_RANGE.strikeoutColor)) {
					textStyle.strikeoutColor = styleRange.strikeoutColor;
				}
				if (!ObjectUtils.equals(styleRange.underline,
						DEFAULT_STYLE_RANGE.underline)) {
					textStyle.underline = styleRange.underline;
				}
				if (!ObjectUtils.equals(styleRange.underlineColor,
						DEFAULT_STYLE_RANGE.underlineColor)) {
					textStyle.underlineColor = styleRange.underlineColor;
				}
				if (!ObjectUtils.equals(styleRange.underlineStyle,
						DEFAULT_STYLE_RANGE.underlineStyle)) {
					textStyle.underlineStyle = styleRange.underlineStyle;
				}
				if (!ObjectUtils.equals(styleRange.data,
						DEFAULT_STYLE_RANGE.data)) {
					textStyle.data = styleRange.data;
				}
			}
		};
	}

	private Stylers() {
	}
}
