package com.bkahlert.nebula.utils.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for a convenient work with the {@link ISelectionService}
 * 
 * @author bkahlert
 */
public class SelectionUtils {
	private SelectionUtils() {
		// no instantiation allowed
	}

	/**
	 * Returns the {@link ISelectionService} of the active
	 * {@link IWorkbenchWindow}.
	 * 
	 * @return
	 */
	public static ISelectionService getSelectionService() {
		return getSelectionService(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow());
	}

	/**
	 * Returns the {@link ISelectionService} of the given
	 * {@link IWorkbenchWindow}.
	 * 
	 * @param window
	 * @return
	 */
	public static ISelectionService getSelectionService(IWorkbenchWindow window) {
		return window.getSelectionService();
	}

	/**
	 * Returns the current selection within the active {@link IWorkbenchWindow}.
	 * 
	 * @return
	 * @see ISelectionService#getSelection()
	 */
	public static ISelection getSelection() {
		return getSelectionService().getSelection();
	}

	/**
	 * Returns the current selection within the given {@link IWorkbenchWindow}.
	 * 
	 * @return
	 * @see ISelectionService#getSelection()
	 */
	public static ISelection getSelection(IWorkbenchWindow window) {
		return getSelectionService(window).getSelection();
	}

	/**
	 * Returns the current selection in the given part within the active
	 * {@link IWorkbenchWindow}.
	 * 
	 * @param partId
	 *            of the part
	 * 
	 * @return
	 * @see ISelectionService#getSelection(String)
	 */
	public static ISelection getSelection(String partId) {
		return getSelectionService().getSelection(partId);
	}

	/**
	 * Returns the current selection in the given part within the given
	 * {@link IWorkbenchWindow}.
	 * 
	 * @param partId
	 *            of the part
	 * 
	 * @return
	 * @see ISelectionService#getSelection(String)
	 */
	public static ISelection getSelection(IWorkbenchWindow window, String partId) {
		return getSelectionService(window).getSelection(partId);
	}

	/**
	 * Returns all selections made in the current perspective within the active
	 * {@link IWorkbenchWindow}.
	 * 
	 * @return
	 */
	public static List<ISelection> getOverallSelections() {
		return getOverallSelections(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow());
	}

	/**
	 * Returns all selections made in the current perspective within the given
	 * {@link IWorkbenchWindow}.
	 * 
	 * @return
	 */
	public static List<ISelection> getOverallSelections(IWorkbenchWindow window) {
		List<ISelection> selections = new ArrayList<ISelection>();

		for (IWorkbenchPage workbenchPage : window.getPages()) {
			for (IEditorReference editorReference : workbenchPage
					.getEditorReferences()) {
				ISelection selection = getSelection(editorReference.getId());
				if (selection != null) {
					selections.add(selection);
				}
			}

			for (IViewReference viewReference : workbenchPage
					.getViewReferences()) {
				ISelection selection = getSelection(viewReference.getId());
				if (selection != null) {
					selections.add(selection);
				}
			}
		}

		return selections;
	}

	/**
	 * Returns all selections made in the current perspective within all
	 * {@link IWorkbenchWindow}s.
	 * 
	 * @return
	 */
	public static List<ISelection> getWorkbenchSelections() {
		List<ISelection> selections = new ArrayList<ISelection>();

		for (IWorkbenchWindow window : PlatformUI.getWorkbench()
				.getWorkbenchWindows()) {
			selections.addAll(getOverallSelections(window));
		}

		return selections;
	}

	/**
	 * Tries to adapt each selection item to adapter and returns all adapted
	 * items.
	 * 
	 * @param selection
	 * @param adapter
	 *            to adapt each object to
	 * @return
	 */
	public static <Adapter> List<Adapter> getAdaptableObjects(
			ISelection selection, Class<? extends Adapter> adapter) {
		List<Object> objectsToAdapt = new ArrayList<Object>();

		if (selection == null) {
			// do nothing
		} else if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Object structuredSelectionItem : structuredSelection.toArray()) {
				objectsToAdapt.add(structuredSelectionItem);
			}
		} else {
			objectsToAdapt.add(selection);
		}

		return ArrayUtils
				.getAdaptableObjects(objectsToAdapt.toArray(), adapter);
	}

}
