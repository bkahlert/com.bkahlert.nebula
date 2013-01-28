package com.bkahlert.devel.nebula.viewer;

import java.util.Comparator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SortableTableViewer extends TableViewer {

	private GenericColumnViewerComparator genericColumnViewerComparator = new GenericColumnViewerComparator();

	public SortableTableViewer(Table table) {
		super(table);
		super.setComparator(this.genericColumnViewerComparator);
	}

	@Override
	public void setComparator(ViewerComparator comparator) {
		// we manage the comparator on our own
	}

	public TableViewerColumn createColumn(String title, int bound,
			boolean isResizable, boolean isMoveable,
			Comparator<Object> comparator, Class<?>[] comparatorClasses) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(this,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(isResizable);
		column.setMoveable(isMoveable);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sort(column);
			}
		});

		if (comparator != null)
			this.genericColumnViewerComparator.setComparator(
					getColumnNumber(column), comparator, comparatorClasses);

		return viewerColumn;
	}

	public TableViewerColumn createColumn(String title, int bound,
			boolean isResizableAndMovable, Comparator<Object> comparator,
			Class<?>[] comparatorClasses) {
		return this.createColumn(title, bound, isResizableAndMovable,
				isResizableAndMovable, comparator, comparatorClasses);
	}

	public TableViewerColumn createColumn(String title, int bound) {
		return this.createColumn(title, bound, true, true, null, null);
	}

	public TableColumn getColumn(int colNumber) {
		return this.getTable().getColumn(colNumber);
	}

	public int getColumnNumber(TableColumn tableColumn) {
		for (int i = 0; i < this.getTable().getColumnCount(); i++) {
			if (this.getTable().getColumn(i) == tableColumn)
				return i;
		}
		return -1;
	}

	public void sort(TableColumn column) {
		this.genericColumnViewerComparator.setColumn(getColumnNumber(column));
		int dir = this.genericColumnViewerComparator.getDirection();
		SortableTableViewer.this.getTable().setSortDirection(dir);
		SortableTableViewer.this.getTable().setSortColumn(column);
		SortableTableViewer.this.refresh();
	}

	public void sort(int colNumber) {
		this.sort(this.getColumn(colNumber));
	}
}
