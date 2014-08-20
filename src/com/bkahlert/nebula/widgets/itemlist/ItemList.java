package com.bkahlert.nebula.widgets.itemlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.JSONUtils;
import com.bkahlert.nebula.utils.SWTUtils;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;

public class ItemList extends BootstrapBrowser {

	public static interface IItemListListener {
		public void itemHovered(String key, int i, boolean entered);

		public void itemClicked(String key, int i);
	}

	public static class ItemListAdapter implements IItemListListener {
		@Override
		public void itemHovered(String key, int i, boolean entered) {
		}

		@Override
		public void itemClicked(String key, int i) {
		}
	}

	private final List<IItemListListener> itemListListeners = new ArrayList<ItemList.IItemListListener>();
	private double margin = 0.0;
	private double spacing = 0.0;

	public ItemList(Composite parent, int style) {
		super(parent, style);
		this.deactivateNativeMenu();
		this.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerHovered(IAnker anker, boolean entered) {
				String key = anker.getData("itemlist-key");
				if (key == null) {
					return;
				}
				String action = anker.getData("itemlist-action");
				for (IItemListListener itemListListener : ItemList.this.itemListListeners) {
					int num = Integer.valueOf(action);
					itemListListener.itemHovered(key, num, entered);
				}
			}

			@Override
			public void ankerClicked(IAnker anker) {
				String key = anker.getData("itemlist-key");
				if (key == null) {
					return;
				}
				String action = anker.getData("itemlist-action");
				for (IItemListListener itemListListener : ItemList.this.itemListListeners) {
					int num = Integer.valueOf(action);
					itemListListener.itemClicked(key, num);
				}
			}
		});
		this.open(BrowserUtils.getFileUrl(ItemList.class, "html/index.html",
				"?internal=true"), 60000);
		this.setBackground(SWTUtils.getEffectiveBackground(this));
	}

	public void addItem(String id, String title) {
		this.addItem(id, title, ButtonOption.DEFAULT, ButtonSize.DEFAULT,
				ButtonStyle.DROPDOWN, null);
	}

	public void addItem(String id, String title, ButtonOption buttonOption,
			ButtonSize buttonSize, ButtonStyle buttonStyle,
			Collection<String> secondaryActions) {
		this.addItem(id, title, buttonOption.toString(), buttonSize,
				buttonStyle, secondaryActions);
	}

	public void addItem(String id, String title, RGB backgroundColor,
			ButtonSize buttonSize, ButtonStyle buttonStyle,
			Collection<String> secondaryActions) {
		RGB fontColor = ColorSpaceConverter.RGBtoHLS(backgroundColor)
				.getLightness() > .7 ? new RGB(51, 51, 51) : RGB.WHITE;
		RGB borderColor = BootstrapBrowser.getBorderColor(backgroundColor);
		RGB hoverColor = BootstrapBrowser.getHoverColor(backgroundColor);
		RGB hoverborderColor = BootstrapBrowser
				.getHoverBorderColor(backgroundColor);

		// FIXME: Would be better to use the button-variant mixin in Bootstrap's
		// mixins.less

		String className = RandomStringUtils.randomAlphabetic(8);
		String css = "." + className + " {color: " + fontColor.toCssString()
				+ "; background-color: " + backgroundColor.toCssString()
				+ "; border-color: " + borderColor.toCssString() + ";} ."
				+ className + ":hover, ." + className + ":focus, ." + className
				+ ":active, ." + className + ".active, .open>.dropdown-toggle."
				+ className + " { color: " + fontColor.toCssString()
				+ "; background-color: " + hoverColor.toCssString()
				+ "; border-color: " + hoverborderColor.toCssString() + ";}";
		this.injectCss(css);
		this.addItem(id, title, className, buttonSize, buttonStyle,
				secondaryActions);
	}

	public void addItem(String id, String title, String className,
			ButtonSize buttonSize, ButtonStyle buttonStyle,
			Collection<String> secondaryActions) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"btn-group\">");
		sb.append("<a class=\"btn " + className + " " + buttonSize
				+ "\" data-itemlist-key=\"" + id
				+ "\" data-itemlist-action=\"0\">" + title + "</a>");
		if (secondaryActions != null && !secondaryActions.isEmpty()) {
			if (buttonStyle == ButtonStyle.DROPDOWN) {
				sb.append("<a class=\"btn dropdown-toggle " + className + " "
						+ buttonSize + "\" data-toggle=\"dropdown\">");
				sb.append("<span class=\"caret\"></span>");
				sb.append("<span class=\"sr-only\">Toggle Dropdown</span>");
				sb.append("</a>");
				sb.append("<ul class=\"dropdown-menu\" role=\"menu\">");
				int i = 1;
				for (String secondaryAction : secondaryActions) {
					if (secondaryAction == null || secondaryAction.isEmpty()
							|| secondaryAction.equals("-")) {
						sb.append("<li class=\"divider\"></li>");
					} else {
						sb.append("<li><a data-itemlist-key=\"" + id
								+ "\" data-itemlist-action=\"" + i + "\">"
								+ secondaryAction + "</a></li>");
					}
					i++;
				}
				sb.append("</ul>");
			} else {
				int i = 1;
				for (String secondaryAction : secondaryActions) {
					sb.append("<a class=\"btn " + className + " " + buttonSize
							+ "\" data-itemlist-key=\"" + id
							+ "\" data-itemlist-action=\"" + i + "\">"
							+ secondaryAction + "</a>");
					i++;
				}
			}
		}
		sb.append("</div>");

		this.run("$(" + JSONUtils.enquote(sb.toString())
				+ ").appendTo('body');");
	}

	public Future<Void> clear() {
		return this.run("$('body').empty();", IConverter.CONVERTER_VOID);
	}

	public Future<Void> setMargin(double pixels) {
		this.margin = pixels;
		return this.updateLayout();
	}

	public Future<Void> setSpacing(double pixels) {
		this.spacing = pixels;
		return this.updateLayout();
	}

	private Future<Void> updateLayout() {
		return this
				.injectCss("body { margin: "
						+ (this.margin - this.spacing / 2)
						+ "px !important; padding: 0 !important; } .btn-group { margin: "
						+ this.spacing / 2.0 + "px; }");
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		String hex = color != null ? new RGB(color.getRGB()).toHexString()
				: "transparent";
		this.injectCss("body { background-color: " + hex + " !important; }");
	}

	public void addListener(IItemListListener itemListListener) {
		this.itemListListeners.add(itemListListener);
	}

	public void removeListener(IItemListListener itemListListener) {
		this.itemListListeners.remove(itemListListener);
	}
}
