package com.bkahlert.nebula.utils;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;

import com.bkahlert.devel.rcp.selectionUtils.retriever.SelectionRetrieverFactory;

public class DNDUtils {

	/**
	 * Tells the caller if Drag'n'Drop is allowed right now. This can be used to
	 * disallow Drag'n'Drop while some other mouse-down mouse-mown action
	 * happens that would normally trigger the DND mechanism.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static interface Oracle {
		public boolean allowDND();
	}

	/**
	 * Adds drag support within this instance of Eclipse. Allows dragging if at
	 * least one object part of the selection that can be adapted to the given
	 * Class.
	 * 
	 * @param viewer
	 * @param oracle
	 * @param clazz
	 */
	public static <T> void addLocalDragSupport(final StructuredViewer viewer,
			final Oracle oracle, final Class<T> clazz) {
		int operations = DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };
		viewer.addDragSupport(operations, transferTypes,
				new DragSourceListener() {
					@Override
					public void dragStart(DragSourceEvent event) {
						if (oracle.allowDND()
								&& SelectionRetrieverFactory
										.getSelectionRetriever(clazz)
										.getSelection().size() > 0) {
							LocalSelectionTransfer.getTransfer().setSelection(
									viewer.getSelection());
							LocalSelectionTransfer.getTransfer()
									.setSelectionSetTime(
											event.time & 0xFFFFFFFFL);
							event.doit = true;
						} else {
							event.doit = false;
						}
					};

					@Override
					public void dragSetData(DragSourceEvent event) {
						if (!oracle.allowDND()) {
							event.doit = false;
						}

						if (LocalSelectionTransfer.getTransfer()
								.isSupportedType(event.dataType)) {
							event.data = LocalSelectionTransfer.getTransfer()
									.getSelection();
						}
					}

					@Override
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
						LocalSelectionTransfer.getTransfer()
								.setSelectionSetTime(0);
					}
				});
	}

	private DNDUtils() {

	}
}
