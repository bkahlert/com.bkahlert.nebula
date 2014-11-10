package com.bkahlert.nebula.widgets.itemlist;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList.IItemListListener;

public class ItemListViewer extends ContentViewer {

	public interface IButtonLabelProvider {
		public ButtonOption getOption(Object object);

		public ButtonSize getSize(Object object);

		public ButtonStyle getStyle(Object object);
	}

	private ItemList itemList;

	private Map<String, Object> keys = new HashMap<String, Object>();
	private Object clicked = null;

	public ItemListViewer(ItemList itemList) {
		this.itemList = itemList;
		this.itemList.addListener(new IItemListListener() {
			@Override
			public void itemHovered(String key, int i, boolean entered) {
			}

			@Override
			public void itemClicked(String key, int i) {
				clicked = keys.get(key);
				fireSelectionChanged(new SelectionChangedEvent(
						ItemListViewer.this, new StructuredSelection(clicked)));
			}
		});
	}

	@Override
	public Control getControl() {
		return this.itemList;
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(clicked);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		return;
	}

	@Override
	public void refresh() {
		this.itemList.clear();
		this.keys.clear();
		IContentProvider cp = getContentProvider();
		ILabelProvider lp = getLabelProvider() instanceof ILabelProvider ? (ILabelProvider) getLabelProvider()
				: null;
		IButtonLabelProvider blp = null;
		if (lp instanceof IButtonLabelProvider) {
			blp = (IButtonLabelProvider) lp;
		}

		if (cp instanceof IStructuredContentProvider) {
			for (Object obj : ((IStructuredContentProvider) cp)
					.getElements(getInput())) {
				String key = StringUtils.createRandomString(32);
				keys.put(key, obj);
				String text = lp != null ? lp.getText(obj) : obj.toString();
				if (blp != null) {
					this.itemList.addItem(key, text, blp.getOption(obj),
							blp.getSize(obj), blp.getStyle(obj), null);
				} else {
					this.itemList.addItem(key, text);
				}
			}
		}
	}
}
