package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Utility class for manipulation of {@link Viewer Viewers}.
 * <p>
 * Tries to call alternative methods if a special operation is not supported by
 * the viewer. All calls are automatically done in the synchronous SWT thread.
 * 
 * @author bkahlert
 */
public class TreeTableUtils {

	private TreeTableUtils() {
		// no instantiation allowed
	}

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object is of the
	 * given type.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static List<Item> getItemWithDataType(Item[] items, Class<?> clazz) {
		if (items == null) {
			return null;
		}
	
		List<Item> itemsWithDataType = new ArrayList<Item>();
		for (Item item : TreeTableUtils.getAllItems(items)) {
			if (clazz.isInstance(item.getData())) {
				itemsWithDataType.add(item);
			}
		}
	
		return itemsWithDataType;
	}

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object is of the
	 * given type.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static List<Item> getItemWithDataType(Control control, Class<?> clazz) {
		if (control == null) {
			return null;
		}
	
		List<Item> itemsWithDataType = new ArrayList<Item>();
		for (Item item : TreeTableUtils.getAllItems(control)) {
			if (clazz.isInstance(item.getData())) {
				itemsWithDataType.add(item);
			}
		}
	
		return itemsWithDataType;
	}

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object equals the
	 * given one.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static List<Item> getItemWithData(Item[] items, Object data) {
		if (items == null) {
			return null;
		}
	
		List<Item> itemsWithData = new ArrayList<Item>();
		for (Item item : TreeTableUtils.getAllItems(items)) {
			if (data.equals(item.getData())) {
				itemsWithData.add(item);
			}
		}
	
		return itemsWithData;
	}

	/**
	 * Returns all {@link Item}s who's {@link Item#getData()} object equals the
	 * given one.
	 * 
	 * @param items
	 * @param clazz
	 * @return
	 */
	public static List<Item> getItemWithData(Control control, Object data) {
		if (control == null) {
			return null;
		}
	
		List<Item> itemsWithData = new ArrayList<Item>();
		for (Item item : TreeTableUtils.getAllItems(control)) {
			if (data.equals(item.getData())) {
				itemsWithData.add(item);
			}
		}
		return itemsWithData;
	}

	/**
	 * Returns a list that does not only contain the {@link Item}s themselves
	 * but also their child, children's children, etc.
	 * 
	 * @param items
	 * @return
	 */
	public static List<Item> getAllItems(Control control) {
		Item[] items = (control instanceof Tree) ? ((Tree) control).getItems()
				: ((Table) control).getItems();
		return TreeTableUtils.getAllItems(items);
	}

	/**
	 * Returns a list that does not only contain the {@link Item}s themselves
	 * but also their child, children's children, etc.
	 * 
	 * @param items
	 * @return
	 */
	public static List<Item> getAllItems(Item[] items) {
		List<Item> allItems = new ArrayList<Item>();
		for (Item item : items) {
			allItems.add(item);
			if (item instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) item;
				allItems.addAll(TreeTableUtils.listTreeItems(treeItem));
			}
		}
		return allItems;
	}

	/**
	 * Returns a list of all elements contained in the {@link TreeItem}.
	 * <p>
	 * Example:
	 * 
	 * <code>a</code> is root and has children <code>b</code> and <code>e</code>. <code>b</code> has the children <code>c</code> and <code>d</code>.
	 * 
	 * The resulting list contains the elements <code>b</code>, <code>c</code>,
	 * <code>d</code> and <code>e</code> whereas <code>a</code> was the
	 * argument.
	 * 
	 * @param treeItem
	 * @return
	 */
	public static List<TreeItem> listTreeItems(TreeItem treeItem) {
		List<TreeItem> treeItems = new ArrayList<TreeItem>();
		for (TreeItem child : treeItem.getItems()) {
			treeItems.add(child);
			treeItems.addAll(listTreeItems(child));
		}
		return treeItems;
	}

	public static Item getColumn(Control control, int index) {
		int[] order = control instanceof Table ? ((Table) control)
				.getColumnOrder() : ((Tree) control).getColumnOrder();
		for (int j = 0, n = order.length; j < n; j++) {
			if (order[j] == index) {
				Item[] columns = control instanceof Table ? ((Table) control)
						.getColumns() : ((Tree) control).getColumns();
				return columns[j];
			}
		}
		return null;
	}

	public static TableColumn getColumn(Table table, int index) {
		return (TableColumn) getColumn((Control) table, index);
	}

	public static TreeColumn getColumn(Tree tree, int index) {
		return (TreeColumn) getColumn((Control) tree, index);
	}

}
