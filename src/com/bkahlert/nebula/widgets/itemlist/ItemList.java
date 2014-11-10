package com.bkahlert.nebula.widgets.itemlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.JSONUtils;
import com.bkahlert.nebula.utils.colors.ColorSpaceConverter;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;
import com.bkahlert.nebula.widgets.browser.listener.AnkerAdapter;
import com.bkahlert.nebula.widgets.browser.listener.MouseAdapter;

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

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(ItemList.class);

	private final List<IItemListListener> itemListListeners = new ArrayList<ItemList.IItemListListener>();
	private double margin = 0.0;
	private double spacing = 0.0;
	private boolean noWrap;

	public ItemList(Composite parent, int style) {
		super(parent, (style | SWT.INHERIT_FORCE) & ~SWT.HORIZONTAL);
		noWrap = (style & SWT.HORIZONTAL) != 0;
		this.deactivateNativeMenu();
		this.addAnkerListener(new AnkerAdapter() {
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
		});
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void clicked(double x, double y, IElement element) {
				String key = element.getData("itemlist-key");
				if (key == null) {
					return;
				}
				String action = element.getData("itemlist-action");
				for (IItemListListener itemListListener : ItemList.this.itemListListeners) {
					int num = Integer.valueOf(action);
					itemListListener.itemClicked(key, num);
				}
			}
		});
		this.open(BrowserUtils.getFileUrl(ItemList.class, "html/index.html",
				"?internal=true"), 60000);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		if (noWrap) {
			injectCss(".content { white-space: nowrap; }");
			RGB from = new RGB(color.getRGB());
			RGB to = new RGB(color.getRGB());
			from.setAlpha(0.0);
			to.setAlpha(1.0);
			injectCss("body:after { display: block; position: absolute; content: ''; top: 0; right: 0; bottom: 0; width: 50px; background: linear-gradient(to right, "
					+ from.toCssString()
					+ " 0%,"
					+ to.toCssString()
					+ " 80%); })");
		}
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
				+ ").appendTo('.content');");
	}

	public Future<Void> clear() {
		return this.run("$('.content').empty();", IConverter.CONVERTER_VOID);
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
		double contentMargin = this.margin - this.spacing;
		return this.injectCss(".content { margin-top: " + contentMargin
				+ "px !important; margin-bottom: " + contentMargin
				+ "px; padding: 0 !important; } .btn-group { margin-top: "
				+ this.spacing + "px; margin-left: " + this.spacing + "px; }");
	}

	public void addListener(IItemListListener itemListListener) {
		this.itemListListeners.add(itemListListener);
	}

	public void removeListener(IItemListListener itemListListener) {
		this.itemListListeners.remove(itemListListener);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point size = super.computeSize(wHint, hHint, changed);
		size.y -= spacing - 2 - 2 * margin;
		return size;
	}
}
