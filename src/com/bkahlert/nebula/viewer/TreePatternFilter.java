package com.bkahlert.nebula.viewer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.bkahlert.nebula.utils.CellLabelClient;

/**
 * Using a {@link FilteredTree} requires the {@link TreeViewer} to return an
 * {@link ILabelProvider} for the whole thing. If {@link TreeViewerColumn} are
 * used there is no single {@link ILabelProvider}. This {@link PatternFilter}
 * considers all column's {@link ILabelProvider}s.
 * 
 * @author bkahlert
 * 
 */
public class TreePatternFilter extends PatternFilter {

	private final Map<Integer, CellLabelClient> cellLabelClients = new HashMap<Integer, CellLabelClient>();

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		if (!(viewer instanceof TreeViewer)) {
			return true;
		}

		TreeViewer treeViewer = (TreeViewer) viewer;
		int numberOfColumns = treeViewer.getTree().getColumnCount();
		boolean isMatch = false;
		for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
			CellLabelProvider cellLabelProvider = treeViewer
					.getLabelProvider(columnIndex);
			if (!this.cellLabelClients.containsKey(columnIndex)) {
				this.cellLabelClients.put(columnIndex, new CellLabelClient(
						cellLabelProvider));
			}

			CellLabelClient labelClient = this.cellLabelClients
					.get(columnIndex);
			labelClient.setElement(element);
			String labelText = labelClient.getText();
			isMatch |= this.wordMatches(labelText);
		}
		return isMatch;
	}
}