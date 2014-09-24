package com.bkahlert.nebula.viewer;

import java.util.Comparator;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.bkahlert.nebula.utils.DistributionUtils.Width;
import com.bkahlert.nebula.utils.ViewerUtils.FullWidthResizer;

public class SortableTableViewer extends TableViewer {

	private final GenericColumnViewerComparator genericColumnViewerComparator = new GenericColumnViewerComparator();
	private final FullWidthResizer fullWidthResizer;

	public SortableTableViewer(Table table) {
		super(table);
		super.setComparator(this.genericColumnViewerComparator);
		this.fullWidthResizer = new FullWidthResizer(this);
		ColumnViewerToolTipSupport.enableFor(this, ToolTip.NO_RECREATE);
	}

	@Override
	public void setComparator(ViewerComparator comparator) {
		// we manage the comparator on our own
	}

	public TableViewerColumn createColumn(String title, Width width,
			boolean isResizable, boolean isMoveable,
			Comparator<Object> comparator, Class<?>[] comparatorClasses) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(this,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		this.fullWidthResizer.setWidth(this.getTable().getColumnCount() - 1,
				width);
		column.setResizable(isResizable);
		column.setMoveable(isMoveable);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SortableTableViewer.this.sort(column);
			}
		});

		if (comparator != null) {
			this.genericColumnViewerComparator
					.setComparator(this.getColumnNumber(column), comparator,
							comparatorClasses);
		}

		return viewerColumn;
	}

	public TableViewerColumn createColumn(String title, Width width,
			boolean isResizableAndMovable, Comparator<Object> comparator,
			Class<?>[] comparatorClasses) {
		return this.createColumn(title, width, isResizableAndMovable,
				isResizableAndMovable, comparator, comparatorClasses);
	}

	public TableViewerColumn createColumn(String title, Width width) {
		return this.createColumn(title, width, true, true, null, null);
	}

	public TableColumn getColumn(int colNumber) {
		return this.getTable().getColumn(colNumber);
	}

	public int getColumnNumber(TableColumn tableColumn) {
		for (int i = 0; i < this.getTable().getColumnCount(); i++) {
			if (this.getTable().getColumn(i) == tableColumn) {
				return i;
			}
		}
		return -1;
	}

	public void sort(TableColumn column) {
		this.genericColumnViewerComparator.setColumn(this
				.getColumnNumber(column));
		int dir = this.genericColumnViewerComparator.getDirection();
		SortableTableViewer.this.getTable().setSortDirection(dir);
		SortableTableViewer.this.getTable().setSortColumn(column);
		SortableTableViewer.this.refresh();
	}

	public void sort(int colNumber) {
		this.sort(this.getColumn(colNumber));
	}
}
