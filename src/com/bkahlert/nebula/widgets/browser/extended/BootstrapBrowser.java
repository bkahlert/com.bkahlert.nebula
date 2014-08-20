package com.bkahlert.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.bootstrap.BootstrapBrowserExtension;

public class BootstrapBrowser extends JQueryBrowser implements
		IBootstrapBrowser {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(BootstrapBrowser.class);

	/**
	 * Calculates a Bootstrap button's border color based on its background
	 * color.
	 * 
	 * @param backgroundColor
	 * @return
	 */
	public static RGB getBorderColor(RGB backgroundColor) {
		return ColorUtils.addLightness(backgroundColor, -0.05f);
	}

	/**
	 * Calculates a Bootstrap button's hovered border color based on its
	 * background color.
	 * 
	 * @param backgroundColor
	 * @return
	 */
	public static RGB getHoverColor(RGB backgroundColor) {
		return ColorUtils.addLightness(backgroundColor, -0.10f);
	}

	/**
	 * Calculates a Bootstrap button's hovered background color based on its
	 * background color.
	 * 
	 * @param backgroundColor
	 * @return
	 */
	public static RGB getHoverBorderColor(RGB backgroundColor) {
		return ColorUtils.addLightness(backgroundColor, -0.17f);
	}

	public static enum ButtonOption {
		DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER;

		public RGB getColor() {
			switch (this) {
			case DANGER:
				return new RGB(217, 83, 79);
			case DEFAULT:
				return new RGB(255, 255, 255);
			case INFO:
				return new RGB(91, 192, 222);
			case PRIMARY:
				return new RGB(66, 139, 202);
			case SUCCESS:
				return new RGB(92, 184, 92);
			case WARNING:
				return new RGB(240, 173, 78);
			default:
				return null;
			}
		}

		@Override
		public String toString() {
			switch (this) {
			case DANGER:
				return "btn-danger";
			case DEFAULT:
				return "btn-default";
			case INFO:
				return "btn-info";
			case PRIMARY:
				return "btn-primary";
			case SUCCESS:
				return "btn-success";
			case WARNING:
				return "btn-warning";
			default:
				return "";
			}
		}
	}

	public static enum ButtonSize {
		LARGE, DEFAULT, SMALL, EXTRA_SMALL;

		@Override
		public String toString() {
			switch (this) {
			case LARGE:
				return "btn-lg";
			case DEFAULT:
				return "";
			case SMALL:
				return "btn-sm";
			case EXTRA_SMALL:
				return "btn-xs";
			default:
				return "";
			}
		};
	}

	public static enum ButtonStyle {
		HORIZONTAL, DROPDOWN;
	}

	public BootstrapBrowser(Composite parent, int style) {
		this(parent, style, new IBrowserExtension[] {});
	}

	@SuppressWarnings("serial")
	public BootstrapBrowser(Composite parent, int style,
			final IBrowserExtension[] extensions) {
		super(parent, style, new ArrayList<IBrowserExtension>() {
			{
				this.add(new BootstrapBrowserExtension());
				if (extensions != null) {
					this.addAll(Arrays.asList(extensions));
				}
			}
		}.toArray(new IBrowserExtension[0]));
	}

}
