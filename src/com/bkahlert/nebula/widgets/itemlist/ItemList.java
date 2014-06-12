package com.bkahlert.nebula.widgets.itemlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.JSONUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;

public class ItemList extends BootstrapBrowser {

	public static interface IItemListListener {
		public void itemHovered(String key, boolean entered);

		public void itemHovered(String key, int i, boolean entered);

		public void itemClicked(String key);

		public void itemClicked(String key, int i);
	}

	public static class ItemListAdapter implements IItemListListener {
		@Override
		public void itemHovered(String key, boolean entered) {
		}

		@Override
		public void itemHovered(String key, int i, boolean entered) {
		}

		@Override
		public void itemClicked(String key) {
		}

		@Override
		public void itemClicked(String key, int i) {
		}
	}

	private final List<IItemListListener> itemListListeners = new ArrayList<ItemList.IItemListListener>();

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
					if (action == null) {
						itemListListener.itemHovered(key, entered);
					} else {
						int num = Integer.valueOf(action);
						itemListListener.itemHovered(key, num, entered);
					}
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
					if (action == null) {
						itemListListener.itemClicked(key);
					} else {
						int num = Integer.valueOf(action);
						itemListListener.itemClicked(key, num);
					}
				}
			}
		});
		this.open(BrowserUtils.getFileUrl(ItemList.class, "html/index.html",
				"?internal=true"), 60000);
	}

	public void addItem(String id, String title) {
		this.addItem(id, title, ButtonOption.DEFAULT, ButtonSize.DEFAULT,
				ButtonStyle.DROPDOWN, null);
	}

	public void addItem(String id, String title, ButtonOption buttonOption,
			ButtonSize buttonSize, ButtonStyle buttonStyle,
			Collection<String> secondaryActions) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"btn-group\">");
		sb.append("<a class=\"btn " + buttonOption + " " + buttonSize
				+ "\" data-itemlist-key=\"" + id + "\">" + title + "</a>");
		if (secondaryActions != null && !secondaryActions.isEmpty()) {
			if (buttonStyle == ButtonStyle.DROPDOWN) {
				sb.append("<a class=\"btn dropdown-toggle " + buttonOption
						+ " " + buttonSize + "\" data-toggle=\"dropdown\">");
				sb.append("<span class=\"caret\"></span>");
				sb.append("<span class=\"sr-only\">Toggle Dropdown</span>");
				sb.append("</a>");
				sb.append("<ul class=\"dropdown-menu\" role=\"menu\">");
				int i = 0;
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
				int i = 0;
				for (String secondaryAction : secondaryActions) {
					sb.append("<a class=\"btn " + buttonOption + " "
							+ buttonSize + "\" data-itemlist-key=\"" + id
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

	public Future<Void> setSpacing(int pixels) {
		return this.injectCss(".btn-group { margin: " + pixels + "px; }");
	}

	public void addListener(IItemListListener itemListListener) {
		this.itemListListeners.add(itemListListener);
	}

	public void removeListener(IItemListListener itemListListener) {
		this.itemListListeners.remove(itemListListener);
	}
}
