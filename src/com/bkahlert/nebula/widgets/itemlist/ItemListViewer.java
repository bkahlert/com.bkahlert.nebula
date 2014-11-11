package com.bkahlert.nebula.widgets.itemlist;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.nebula.utils.StringUtils;
import com.bkahlert.nebula.utils.colors.RGB;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonOption;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonSize;
import com.bkahlert.nebula.widgets.browser.extended.BootstrapBrowser.ButtonStyle;
import com.bkahlert.nebula.widgets.itemlist.ItemList.IItemListListener;

public class ItemListViewer extends ContentViewer {

	public interface IButtonLabelProvider {
		/**
		 * Returns the nature of the button. The return value is in conflict
		 * with {@link #getColor(Object)} and takes precedence if anything not
		 * <code>null</code> is returned.
		 *
		 * @param object
		 * @return
		 */
		public ButtonOption getOption(Object object);

		/**
		 * Returns the color of the button. This method is only called if
		 * {@link #getOption(Object)} returns <code>null</code>.
		 *
		 * @param object
		 * @return
		 */
		public RGB getColor(Object object);

		public ButtonSize getSize(Object object);

		public ButtonStyle getStyle(Object object);
	}

	public static class ButtonLabelProvider extends LabelProvider implements
	IButtonLabelProvider {
		@Override
		public String getText(Object element) {
			return element.toString();
		}

		@Override
		public ButtonOption getOption(Object object) {
			return ButtonOption.DEFAULT;
		}

		@Override
		public RGB getColor(Object object) {
			return null;
		}

		@Override
		public ButtonSize getSize(Object object) {
			return ButtonSize.DEFAULT;
		}

		@Override
		public ButtonStyle getStyle(Object object) {
			return ButtonStyle.HORIZONTAL;
		}
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
			Object[] elements = ((IStructuredContentProvider) cp)
					.getElements(getInput());
			if (elements != null) {
				for (Object obj : elements) {
					String key = StringUtils.createRandomString(32);
					keys.put(key, obj);
					String text = lp != null ? lp.getText(obj) : obj.toString();
					if (blp != null) {
						ButtonOption option = blp.getOption(obj);
						if (option != null) {
							this.itemList.addItem(key, text, option,
									blp.getSize(obj), blp.getStyle(obj), null);
						} else {
							this.itemList.addItem(key, text, blp.getColor(obj),
									blp.getSize(obj), blp.getStyle(obj), null);
						}
					} else {
						this.itemList.addItem(key, text);
					}
				}
			}
		}
	}
}
