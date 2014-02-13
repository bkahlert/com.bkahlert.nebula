package com.bkahlert.devel.nebula.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.bkahlert.nebula.utils.DistributionUtils;
import com.bkahlert.nebula.utils.DistributionUtils.AbsoluteWidth;
import com.bkahlert.nebula.utils.DistributionUtils.Width;

/**
 * Utility class for manipulation of {@link Viewer Viewers}.
 * <p>
 * Tries to call alternative methods if a special operation is not supported by
 * the viewer. All calls are automatically done in the synchronous SWT thread.
 * <p>
 * <b>Example 1:</b><br/>
 * If you try to update a specific element in a viewer and the viewer does not
 * support updating single elements, the whole viewer will be refreshed.
 * <p>
 * <b>Example 2:</b><br/>
 * One element of your model has been removed. For performance reasons you don't
 * want to refresh the whole viewer but manually remove the element from the
 * viewer in order to reflect the model. If your viewer supports this action the
 * element is removed. Otherwise the viewer is advised to reload the model.
 * 
 * @author bkahlert
 */
public class ViewerUtils {

	public static class FullWidthResizer {
		private final ColumnViewer columnViewer;

		private final Map<Integer, Width> numbers = new HashMap<Integer, Width>();

		private final Listener resizeListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				FullWidthResizer.this.resize();
			}
		};

		public FullWidthResizer(ColumnViewer columnViewer) {
			this.columnViewer = columnViewer;
			this.columnViewer.getControl().addListener(SWT.Resize,
					this.resizeListener);
		}

		public void setWidth(int column, Width width) {
			this.numbers.put(column, width);
			this.resize();
		}

		public void resize() {
			if (this.columnViewer != null
					&& this.columnViewer.getControl() != null
					&& !this.columnViewer.getControl().isDisposed()) {
				Control control = this.columnViewer.getControl();
				if (control instanceof Tree) {
					Tree tree = (Tree) control;
					Width[] input = new Width[tree.getColumnCount()];
					for (int i = 0; i < input.length; i++) {
						input[i] = this.numbers.containsKey(i) ? this.numbers
								.get(i) : new AbsoluteWidth(
								Width.DEFAULT_MIN_WIDTH);
					}
					double[] distribution = DistributionUtils.distribute(input,
							tree.getClientArea().width);
					for (int i = 0; i < distribution.length; i++) {
						tree.getColumn(i).setWidth(
								(int) Math.round(distribution[i]));
					}
				} else if (control instanceof Table) {
					Table table = (Table) control;
					Width[] input = new Width[table.getColumnCount()];
					for (int i = 0; i < input.length; i++) {
						input[i] = this.numbers.containsKey(i) ? this.numbers
								.get(i) : new AbsoluteWidth(
								Width.DEFAULT_MIN_WIDTH);
					}
					double[] distribution = DistributionUtils.distribute(input,
							table.getClientArea().width);
					for (int i = 0; i < distribution.length; i++) {
						table.getColumn(i).setWidth(
								(int) Math.round(distribution[i]));
					}
				}
			}
		}

		public void dispose() {
			if (this.columnViewer != null
					&& this.columnViewer.getControl() != null
					&& !this.columnViewer.getControl().isDisposed()) {
				this.columnViewer.getControl().removeListener(SWT.Resize,
						this.resizeListener);
			}
		}
	}

	private ViewerUtils() {
		// no instantiation allowed
	}

	/**
	 * Sets a viewer's input and makes sure it runs in the SWT thread
	 * 
	 * @param viewer
	 * @param input
	 * 
	 * @see Viewer#setInput(Object)
	 */
	public static void setInput(final Viewer viewer, final Object input) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				viewer.setInput(input);
			}
		});
	}

	/**
	 * Add the a new element to a given element in a viewer and makes sure it
	 * runs in the SWT thread. Runs a refresh in case the viewer does not
	 * support additions.
	 * 
	 * @param viewer
	 * @param parentElementOrTreePath
	 * @param childElement
	 * 
	 * @see StructuredViewer#refresh(boolean)
	 */
	public static void add(final Viewer viewer,
			final Object parentElementOrTreePath, final Object childElement) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.add(parentElementOrTreePath, childElement);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Add the new elements to a given element in a viewer and makes sure it
	 * runs in the SWT thread. Runs a refresh in case the viewer does not
	 * support additions.
	 * 
	 * @param viewer
	 * @param parentElementOrTreePath
	 * @param childElements
	 * 
	 * @see StructuredViewer#refresh(boolean)
	 */
	public static void add(final Viewer viewer,
			final Object parentElementOrTreePath, final Object[] childElements) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.add(parentElementOrTreePath, childElements);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Removes an existing element from a viewer and makes sure it runs in the
	 * SWT thread. Runs a refresh in case the viewer does not support removals.
	 * 
	 * @param viewer
	 * @param elementsOrTreePaths
	 * 
	 * @see StructuredViewer#refresh(boolean)
	 */
	public static void remove(final Viewer viewer,
			final Object elementsOrTreePaths) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.remove(elementsOrTreePaths);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Removes existing elements from a viewer and makes sure it runs in the SWT
	 * thread. Runs a refresh in case the viewer does not support removals.
	 * 
	 * @param viewer
	 * @param elementsOrTreePaths
	 * 
	 * @see StructuredViewer#refresh(boolean)
	 */
	public static void remove(final Viewer viewer,
			final Object[] elementsOrTreePaths) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.remove(elementsOrTreePaths);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Updates a viewer's element and makes sure it runs in the SWT thread. Runs
	 * a refresh in case the viewer does not support updates.
	 * 
	 * @param viewer
	 * @param element
	 * @param properties
	 * 
	 * @see StructuredViewer#update(Object, String[])
	 */
	public static void update(final Viewer viewer, final Object element,
			final String[] properties) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof StructuredViewer) {
					StructuredViewer structuredViewer = (StructuredViewer) viewer;
					structuredViewer.update(element, properties);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Updates a viewer's elements and makes sure it runs in the SWT thread.
	 * Runs a refresh in case the viewer does not support updates.
	 * 
	 * @param viewer
	 * @param elements
	 * @param properties
	 * 
	 * @see StructuredViewer#update(Object[], String[])
	 */
	public static void update(final Viewer viewer, final Object[] elements,
			final String[] properties) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof StructuredViewer) {
					StructuredViewer structuredViewer = (StructuredViewer) viewer;
					structuredViewer.update(elements, properties);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Refreshes a viewer's display and makes sure it runs in the SWT thread.
	 * 
	 * @param viewer
	 * @param updateLabels
	 * 
	 * @see Viewer#refresh()
	 * @see StructuredViewer#refresh(boolean)
	 */
	public static void refresh(final Viewer viewer, final boolean updateLabels) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof StructuredViewer) {
					StructuredViewer structuredViewer = (StructuredViewer) viewer;
					structuredViewer.refresh(updateLabels);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * Refreshes a viewer's display and makes sure it runs in the SWT thread.
	 * 
	 * @param viewer
	 * @param element
	 * @param updateLabels
	 * 
	 * @see Viewer#refresh()
	 * @see StructuredViewer#refresh(Object, boolean)
	 */
	public static void refresh(final Viewer viewer, final Object element,
			final boolean updateLabels) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof StructuredViewer) {
					StructuredViewer structuredViewer = (StructuredViewer) viewer;
					structuredViewer.refresh(element, updateLabels);
				} else {
					viewer.refresh();
				}
			}
		});
	}

	/**
	 * @see AbstractTreeViewer#expandToLevel(int)
	 */
	public static void expandToLevel(final Viewer viewer, final int level) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.expandToLevel(level);
				}
			}
		});
	}

	/**
	 * @see AbstractTreeViewer#expandToLevel(int)
	 */
	public static void expandToLevel(final Viewer viewer,
			final Object elementOrTreePath, final int level) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.expandToLevel(elementOrTreePath, level);
				}
			}
		});
	}

	/**
	 * If supported by the viewer expands all elements and makes sure it runs in
	 * the SWT thread.
	 * 
	 * @param viewer
	 * 
	 * @see AbstractTreeViewer#expandAll()
	 */
	public static void expandAll(final Viewer viewer) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.expandAll();
				}
			}
		});
	}

	/**
	 * @see AbstractTreeViewer#expandToLevel(int)
	 */
	public static void expandAll(final Viewer viewer,
			final Object elementOrTreePath) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null || viewer.getControl().isDisposed()) {
					return;
				}

				if (viewer instanceof AbstractTreeViewer) {
					AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;
					treeViewer.expandToLevel(elementOrTreePath,
							Integer.MAX_VALUE);
				}
			}
		});
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
		for (Item item : ViewerUtils.getAllItems(items)) {
			if (clazz.isInstance(item.getData())) {
				itemsWithDataType.add(item);
			}
		}

		return itemsWithDataType;
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
		return ViewerUtils.getAllItems(items);
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
				allItems.addAll(ViewerUtils.listTreeItems(treeItem));
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

	/**
	 * Merges an array of {@link TreePath}s to one {@link TreePath}.
	 * 
	 * Example: {@link TreePath}s
	 * 
	 * <pre>
	 * A<br/>
	 * | -B
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 * C<br/>
	 * | -D
	 * </pre>
	 * 
	 * become
	 * 
	 * <pre>
	 * A<br/>
	 * | -B<br/>
	 *    | -C<br/>
	 *       | -D
	 * </pre>
	 * 
	 * @param treePaths
	 * @return
	 */
	public static TreePath merge(TreePath... treePaths) {
		ArrayList<Object> segments = new ArrayList<Object>();
		for (TreePath treePath : treePaths) {
			for (int i = 0; i < treePath.getSegmentCount(); i++) {
				segments.add(treePath.getSegment(i));
			}
		}
		return new TreePath(segments.toArray());
	}

	public static Rectangle getBounds(ViewerColumn column) {
		int index = ViewerUtils.getIndex(column);
		Control control = column.getViewer().getControl();

		int x = 0;
		int w;
		if (control instanceof Table) {
			for (int i = 0; i < index; i++) {
				x += ViewerUtils.getColumn((Table) control, i).getWidth();
			}
			w = ViewerUtils.getColumn((Table) control, index).getWidth();
		} else {
			for (int i = 0; i < index; i++) {
				x += ViewerUtils.getColumn((Tree) control, i).getWidth();
			}
			w = ViewerUtils.getColumn((Tree) control, index).getWidth();
			;
		}
		return new Rectangle(x, 0, w, control.getBounds().height);
	}

	public static int getIndex(ViewerColumn viewerColumn) {
		Control control = viewerColumn.getViewer().getControl();
		Item column = viewerColumn instanceof TableViewerColumn ? ((TableViewerColumn) viewerColumn)
				.getColumn() : ((TreeViewerColumn) viewerColumn).getColumn();
		Item[] columns = control instanceof Table ? ((Table) control)
				.getColumns() : ((Tree) control).getColumns();
		for (int i = 0, m = columns.length; i < m; i++) {
			if (columns[i] == column) {
				int[] order = control instanceof Table ? ((Table) control)
						.getColumnOrder() : ((Tree) control).getColumnOrder();
				for (int j = 0, n = order.length; j < n; j++) {
					if (order[j] == i) {
						return j;
					}
				}
			}
		}
		return -1;
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

	public static void refresh(final Viewer viewer) {
		if (viewer != null) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});

		}
	}
}
