package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.utils.colors.RGB;

public class Stylers {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Stylers.class);

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

	public static Styler ITALIC_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontUtils.ITALIC_FONT;
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

	/**
	 * Creates a {@link Styler} that combines the styles of the given
	 * {@link Styler}s.
	 * <p>
	 * The combination is smart. For example a bold {@link Styler} will make the
	 * resulting {@link Styler} bold without affecting the font size or italic
	 * style. Colors are mixed and a {@link Styler} making the font smaller will
	 * make the resulting font smaller.
	 *
	 * @param stylers
	 * @return
	 */
	public static Styler combine(final Styler... stylers) {
		return new Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				for (Styler styler : stylers) {
					if (styler != null) {
						// styler.applyStyles(textStyle);
						mergeApplyStyles(styler, textStyle);
					}
				}
			}
		};
	}

	/**
	 * Applies the {@link Styler} to the {@link TextStyle} is a smart fashion.
	 * Settings are not simply overwritten but really combined. E.g. small +
	 * large text will result is normal size font.
	 *
	 * @param styler
	 * @param textStyle
	 */
	private static void mergeApplyStyles(Styler styler, TextStyle textStyle) {
		TextStyle reference = new TextStyle();
		TextStyle dummy = new TextStyle();
		styler.applyStyles(dummy);
		if (!ObjectUtils.equals(reference.background, dummy.background)) {
			textStyle.background = merge(textStyle.background, dummy.background);
		}
		if (!ObjectUtils.equals(reference.borderColor, dummy.borderColor)) {
			textStyle.borderColor = merge(textStyle.borderColor,
					dummy.borderColor);
		}
		if (!ObjectUtils.equals(reference.borderStyle, dummy.borderStyle)) {
			textStyle.borderStyle = dummy.borderStyle;
		}
		if (!ObjectUtils.equals(reference.font, dummy.font)) {
			textStyle.font = merge(textStyle.font, dummy.font);
		}
		if (!ObjectUtils.equals(reference.foreground, dummy.foreground)) {
			textStyle.foreground = merge(textStyle.foreground, dummy.foreground);
		}
		if (!ObjectUtils.equals(reference.metrics, dummy.metrics)) {
			textStyle.metrics = dummy.metrics;
		}
		if (!ObjectUtils.equals(reference.rise, dummy.rise)) {
			textStyle.rise = (textStyle.rise + dummy.rise) / 2;
		}
		if (!ObjectUtils.equals(reference.strikeout, dummy.strikeout)) {
			textStyle.strikeout = dummy.strikeout;
		}
		if (!ObjectUtils.equals(reference.strikeoutColor, dummy.strikeoutColor)) {
			textStyle.strikeoutColor = merge(textStyle.strikeoutColor,
					dummy.strikeoutColor);
		}
		if (!ObjectUtils.equals(reference.underline, dummy.underline)) {
			textStyle.underline = dummy.underline;
		}
		if (!ObjectUtils.equals(reference.underlineColor, dummy.underlineColor)) {
			textStyle.underlineColor = merge(textStyle.underlineColor,
					dummy.underlineColor);
		}
		if (!ObjectUtils.equals(reference.underlineStyle, dummy.underlineStyle)) {
			textStyle.underlineStyle = dummy.underlineStyle;
		}
	}

	private static final Map<RGB, Color> colors = new HashMap<RGB, Color>();

	private static Color merge(Color color1, Color color2) {
		if (color1 == null) {
			return color2;
		}
		if (color2 == null) {
			return color1;
		}
		RGB merged = new RGB(color1.getRGB()).mix(new RGB(color2.getRGB()), .5);
		if (!colors.containsKey(merged)) {
			colors.put(merged,
					new Color(Display.getCurrent(), merged.toClassicRGB()));
		}
		return colors.get(merged);
	}

	private static final Map<FontData, Font> fonts = new HashMap<FontData, Font>();

	private static Font merge(Font font1, Font font2) {
		if (font1 == null) {
			return font2;
		}
		if (font2 == null) {
			return font1;
		}
		FontData fontData[] = FontUtils.merge(font1.getFontData(),
				font2.getFontData());
		if (!fonts.containsKey(fontData[0])) {
			fonts.put(fontData[0], new Font(Display.getCurrent(), fontData));
		}
		return fonts.get(fontData[0]);
	}

	/**
	 * Formats the given string as if not the system's default was the base
	 * style but the one defined by the given {@link Styler}.
	 * <p>
	 * Example: if the {@link Styler}'s format was bold and blue, the string
	 * will be bold and blue tinted.
	 *
	 * @param string
	 * @param baseStyler
	 * @return
	 */
	public static StyledString rebase(StyledString string, Styler baseStyler) {
		if (baseStyler == null) {
			return string;
		}
		StyleRange[] styleRanges = getExpandedStyleRanges(string);
		for (StyleRange styleRange : styleRanges) {
			string.setStyle(styleRange.start, styleRange.length,
					combine(baseStyler, createFrom(styleRange)));
		}
		return string;
	}

	private static StyleRange[] getExpandedStyleRanges(StyledString string) {
		StyleRange[] styleRanges = string.getStyleRanges();
		List<StyleRange> expandedStyleRanges = new ArrayList<StyleRange>();
		int filledUntil = 0;
		for (StyleRange styleRange : styleRanges) {
			// fill gaps
			if (styleRange.start > filledUntil) {
				StyleRange gap = new StyleRange(DEFAULT_STYLE_RANGE);
				gap.start = filledUntil;
				gap.length = styleRange.start - filledUntil;
				gap.font = FontUtils.SYSTEM_FONT;
				expandedStyleRanges.add(gap);
			}
			expandedStyleRanges.add(styleRange);
			filledUntil = styleRange.start + styleRange.length;
		}
		// fill up string
		if (filledUntil < string.length()) {
			StyleRange gap = new StyleRange(DEFAULT_STYLE_RANGE);
			gap.start = filledUntil;
			gap.length = string.length() - filledUntil;
			expandedStyleRanges.add(gap);
		}
		return expandedStyleRanges.toArray(new StyleRange[0]);
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
		if (baseStyler != null) {
			return baseString.append(rebase(clone(appendString), baseStyler));
		} else {
			return baseString.append(appendString);
		}
	}

	/**
	 * Creates a new {@link StyledString} based on the given string starting and
	 * ending at the given position.
	 *
	 * @param string
	 * @param beginIndex
	 *            inclusive
	 * @param endIndex
	 *            exclusive
	 * @return
	 */
	public static StyledString substring(StyledString string, int beginIndex,
			int endIndex) {
		StyledString clone = new StyledString(string.getString().substring(
				beginIndex, endIndex));
		for (StyleRange styleRange : string.getStyleRanges()) {
			if (styleRange.start < endIndex) {
				int start = styleRange.start - beginIndex;
				int end = start + styleRange.length;
				if (start < 0) {
					start = 0;
				}
				if (end > clone.length()) {
					end = clone.length();
				}
				int length = end - start;
				if (length > 0) {
					clone.setStyle(start, length, createFrom(styleRange));
				}
			}
		}
		return clone;
	}

	/**
	 * Shortens the given string if its length exceeds the maximum length. In
	 * this case the string is shortened and the fill string is appended (using
	 * the string's style). The returned string will be of length max.
	 *
	 * @param string
	 * @param maxCharacters
	 * @param append
	 * @return
	 */
	public static StyledString shorten(StyledString string, int maxCharacters,
			String append) {
		if (string.length() > maxCharacters) {
			if (append == null) {
				append = "";
			}
			StyleRange[] ranges = getExpandedStyleRanges(string);
			return substring(string, 0, maxCharacters - append.length())
					.append(append, createFrom(ranges[ranges.length - 1]));
		} else {
			return clone(string);
		}
	}

	/**
	 * Shortens the given string if its length exceeds the maximum length. In
	 * this case the string is shortened and the fill string is appended (using
	 * the string's style). The returned string will have min(numContainedWords,
	 * minWords) words.
	 *
	 * @param styledText
	 * @param maxCharacters
	 * @param minWords
	 * @param string
	 * @return
	 */
	public static StyledString shorten(StyledString styledText,
			int maxCharacters, int minWords, String string) {
		int numWords = styledText.getString().split("\\s+").length;
		if (numWords > minWords) {
			numWords = minWords;
		}
		String regex = numWords > 0 ? "[^\\s-]+" : "";
		for (int i = 1; i < numWords; i++) {
			regex += "\\s+[^\\s-]+";
		}
		regex = "(" + regex + ").*?";
		Matcher matcher = Pattern.compile(regex)
				.matcher(styledText.getString());
		Assert.isTrue(matcher.matches());
		StyledString start = substring(styledText, matcher.start(1),
				matcher.end(1));
		if (matcher.end(1) == styledText.length()) {
			return start;
		}
		StyledString end = shorten(styledText, maxCharacters, string);
		try {
			end = substring(end, matcher.end(1), styledText.length());
		} catch (Exception e) {
			end = substring(end, end.length() - string.length(), end.length());
		}
		return append(start, end);
	}

	/**
	 * Clones a {@link StyledString}.
	 *
	 * @param string
	 * @return
	 */
	public static StyledString clone(StyledString string) {
		return substring(string, 0, string.length());
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
