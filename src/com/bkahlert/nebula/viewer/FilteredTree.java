package com.bkahlert.nebula.viewer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.ExecUtils;

/**
 * This extensions of the existing {@link org.eclipse.ui.dialogs.FilteredTree}
 * has two advantages:
 * <ol>
 * <li>The filter takes all columns into account. The original implementation
 * only considered the {@link TreeViewer}'s global {@link ILabelProvider}.</li>
 * <li>The expanded elements state is restored after search has finished.</li>
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public class FilteredTree extends org.eclipse.ui.dialogs.FilteredTree {

	/**
	 * Using a {@link FilteredTree} requires the {@link TreeViewer} to return an
	 * {@link ILabelProvider} for the whole thing. If {@link TreeViewerColumn}
	 * are used there is no single {@link ILabelProvider}. This
	 * {@link PatternFilter} considers all column's {@link ILabelProvider}s.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class TreePatternFilter extends PatternFilter {

		// DIRTY this field is only kept here since we need it in the
		// construction of the viewer. This happens in the super constructor,
		// that's why there's no chance to save the reference before it is
		// actually used.
		private final TreeViewerFactory factory;

		private final Map<Integer, CellLabelClient> cellLabelClients = new HashMap<Integer, CellLabelClient>();

		public TreePatternFilter(TreeViewerFactory factory) {
			this.factory = factory;
		}

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

	public static interface TreeViewerFactory {
		public TreeViewer create(Composite parent, int style);
	}

	private static final Logger LOGGER = Logger.getLogger(FilteredTree.class);

	public FilteredTree(Composite parent, int treeStyle,
			TreeViewerFactory factory) {
		super(parent, treeStyle, new TreePatternFilter(factory), true);
	}

	@Override
	protected WorkbenchJob doCreateRefreshJob() {
		WorkbenchJob job = super.doCreateRefreshJob();
		job.addJobChangeListener(new JobChangeAdapter() {
			private TreePath[] expanded = null;

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				try {
					ExecUtils.syncExec(new Runnable() {
						@Override
						public void run() {
							Text text = FilteredTree.this.getFilterControl();
							if (text != null && !text.isDisposed()
									&& !text.getText().isEmpty()
									&& expanded == null) {
								expanded = FilteredTree.this.getViewer()
										.getExpandedTreePaths();
							}
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error saving expanded state of "
							+ FilteredTree.this.getViewer());
				}
				super.aboutToRun(event);
			}

			@Override
			public void done(final IJobChangeEvent event) {
				super.done(event);
				final Text text = FilteredTree.this.getFilterControl();
				try {
					ExecUtils.syncExec(new Runnable() {
						@Override
						public void run() {
							if (text.getText() != null && !text.isDisposed()
									&& text.getText().isEmpty()) {
								if (expanded != null
										&& (event.getResult() == Status.OK_STATUS)) {
									FilteredTree.this.getViewer()
											.setExpandedTreePaths(expanded);
									expanded = null;
								}
							}
						}
					});
				} catch (Exception e) {
					LOGGER.error("Error filtering Tree", e);
				}

			}
		});
		return job;
	}

	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		return ((TreePatternFilter) this.getPatternFilter()).factory.create(
				parent, style);
	};

}
