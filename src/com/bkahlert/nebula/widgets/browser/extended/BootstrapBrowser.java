package com.bkahlert.nebula.widgets.browser.extended;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.widgets.browser.extended.extensions.IBrowserExtension;
import com.bkahlert.nebula.widgets.browser.extended.extensions.bootstrap.BootstrapBrowserExtension;

public class BootstrapBrowser extends JQueryBrowser implements
		IBootstrapBrowser {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger
			.getLogger(BootstrapBrowser.class);

	public static enum ButtonOption {
		DEFAULT, PRIMARY, SUCCESS, INFO, WARNING, DANGER;

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
