package com.bkahlert.nebula.viewer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.TreeTableUtils;

/**
 * Generic {@link ViewerComparator} for use in conjunction with
 * {@link ColumnViewer}s like {@link TableViewer}s or {@link TreeViewer}s.
 * 
 * @author bkahlert
 */
public class GenericColumnViewerComparator extends ViewerComparator {

	/**
	 * This map uses 2 keys.
	 * <ol>
	 * <li>number of the column</li>
	 * <li>class the comparator is responsible for and may expect</li>
	 * </ol>
	 */
	private final MultiKeyMap comparators = new MultiKeyMap();

	protected int propertyIndex;
	protected static final int DESCENDING = 1;
	protected int direction = DESCENDING;

	// TODO: irgendwann mal caches leeren
	private final MultiKeyMap labelCache = new MultiKeyMap();
	private final MultiKeyMap resultCache = new MultiKeyMap();
	private final Map<Integer, CellLabelClient> cellLabelClients = new HashMap<Integer, CellLabelClient>();

	public GenericColumnViewerComparator() {
		this.propertyIndex = -1;
		this.direction = DESCENDING;
	}

	public int getDirection() {
		return this.direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			this.direction = 1 - this.direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			this.direction = 0;
		}
	}

	@Override
	public int compare(Viewer viewer, final Object e1, Object e2) {
		int rc = 0;

		if (this.propertyIndex < 0) {
			return rc;
		}

		if (!resultCache.containsKey(viewer, this.propertyIndex, e1, e2)) {
			if (e1.getClass().equals(e2.getClass())
					&& this.comparators.containsKey(this.propertyIndex,
							e1.getClass())) {
				@SuppressWarnings("unchecked")
				Comparator<Object> customComparator = (Comparator<Object>) this.comparators
						.get(this.propertyIndex, e1.getClass());
				rc = customComparator.compare(e1, e2);
			} else {
				String str1 = null;
				if (!this.labelCache
						.containsKey(viewer, this.propertyIndex, e1)) {
					str1 = getLabel((ColumnViewer) viewer, e1);
					this.labelCache.put(viewer, this.propertyIndex, e1, str1);
				} else {
					str1 = (String) this.labelCache.get(viewer,
							this.propertyIndex, e1);
				}

				String str2 = null;
				if (!this.labelCache
						.containsKey(viewer, this.propertyIndex, e2)) {
					str2 = getLabel((ColumnViewer) viewer, e2);
					this.labelCache.put(viewer, this.propertyIndex, e2, str2);
				} else {
					str2 = (String) this.labelCache.get(viewer,
							this.propertyIndex, e2);
				}

				try {
					rc = new Integer(Integer.parseInt(str1)).compareTo(Integer
							.parseInt(str2));
				} catch (Exception e) {
					rc = str1.compareTo(str2);
				}
			}
			this.resultCache.put(viewer, this.propertyIndex, e1, e2, rc);
		} else {
			rc = (Integer) this.resultCache.get(viewer, this.propertyIndex, e1,
					e2);
		}
		if (this.direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	private String getLabel(ColumnViewer columnViewer, Object e) {
		String label = null;

		if (!cellLabelClients.containsKey(propertyIndex)) {
			CellLabelClient cellLabelClient = null;
			CellLabelProvider cellLabelProvider = columnViewer
					.getLabelProvider(propertyIndex);
			if (cellLabelProvider != null) {
				cellLabelClient = new CellLabelClient(cellLabelProvider);
			}
			cellLabelClients.put(propertyIndex, cellLabelClient);
		}

		CellLabelClient cellLabelClient = cellLabelClients.get(propertyIndex);
		if (cellLabelClient != null) {
			cellLabelClient.setElement(e);
			label = cellLabelClient.getText();
		} else {
			// otherwise try to read the item's text
			if (columnViewer.getControl() instanceof Table) {
				TableItem tableItem = getTableItemByElement(columnViewer, e);
				if (tableItem != null) {
					label = tableItem.getText(propertyIndex);
				}
			} else if (columnViewer.getControl() instanceof Tree) {
				TreeItem treeItem = getTreeItemByElement(columnViewer, e);
				if (treeItem != null) {
					label = treeItem.getText(propertyIndex);
				}
			}
		}

		return label != null ? label : e.toString();
	}

	public void setComparator(int colNumber, Comparator<Object> comparator,
			Class<?>[] comparatorClasses) {
		for (Class<?> comparatorClass : comparatorClasses) {
			comparators.put(colNumber, comparatorClass, comparator);
		}
	}

	private static TableItem getTableItemByElement(ColumnViewer columnViewer,
			Object e) {
		if (columnViewer.getControl() instanceof Table) {
			Table table = (Table) columnViewer.getControl();
			for (TableItem item : table.getItems()) {
				if (dataContainsPayload(e, item)) {
					return item;
				}
			}
		}
		return null;
	}

	private static TreeItem getTreeItemByElement(ColumnViewer columnViewer,
			Object e) {
		if (columnViewer.getControl() instanceof Tree) {
			Tree tree = (Tree) columnViewer.getControl();
			for (Item item : TreeTableUtils.getAllItems(tree.getItems())) {
				if (dataContainsPayload(e, item)) {
					return (TreeItem) item;
				}
			}
		}
		return null;
	}

	private static boolean dataContainsPayload(Object payload, Item item) {
		if (item != null && item.getData() != null) {
			if (item.getData().equals(payload)) {
				return true;
			}
			if (item.getData() instanceof Object[]) {
				for (Object date : (Object[]) item.getData()) {
					if (date.equals(payload)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}