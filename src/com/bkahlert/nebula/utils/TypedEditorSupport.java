package com.bkahlert.nebula.utils;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

public abstract class TypedEditorSupport<T> extends EditingSupport {

	private static final Logger LOGGER = Logger
			.getLogger(TypedEditorSupport.class);

	private final Class<T> clazz;

	public TypedEditorSupport(ColumnViewer viewer, Class<T> clazz) {
		super(viewer);
		this.clazz = clazz;
	}

	@Override
	protected final boolean canEdit(Object element) {
		T uri = AdapterUtils.adapt(element, this.clazz);
		if (uri != null) {
			try {
				return this.getCellEditor(element) != null
						&& this.getValue(element) != null;
			} catch (Exception e) {
				LOGGER.error("Error retrieving canEdit for " + element, e);
				return false;
			}
		} else {
			return false;
		}
	}

	protected abstract CellEditor getCellEditor(T element, Composite composite)
			throws Exception;

	@Override
	protected final CellEditor getCellEditor(Object element) {
		T uri = AdapterUtils.adapt(element, this.clazz);
		if (uri != null) {
			try {
				Composite composite = null;
				if (this.getViewer() instanceof TableViewer) {
					composite = ((TableViewer) this.getViewer()).getTable();
				}
				if (this.getViewer() instanceof TreeViewer) {
					composite = ((TreeViewer) this.getViewer()).getTree();
				}
				return this.getCellEditor(uri, composite);
			} catch (Exception e) {
				LOGGER.error("Error retrieving cell editor for " + element, e);
				return null;
			}
		} else {
			return null;
		}
	}

	protected abstract Object getInitValue(T element) throws Exception;

	@Override
	protected final Object getValue(Object element) {
		T uri = AdapterUtils.adapt(element, this.clazz);
		if (uri != null) {
			try {
				return this.getInitValue(uri);
			} catch (Exception e) {
				LOGGER.error("Error retrieving value for " + element, e);
				return null;
			}
		} else {
			return null;
		}
	}

	protected abstract void setEditedValue(T element, Object value)
			throws Exception;

	@Override
	protected final void setValue(Object element, Object value) {
		T uri = AdapterUtils.adapt(element, this.clazz);
		if (uri != null) {
			try {
				this.setEditedValue(uri, value);
			} catch (Exception e) {
				LOGGER.error("Error retrieving value for " + element, e);
			}
		}
	}

}
