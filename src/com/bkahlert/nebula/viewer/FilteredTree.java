package com.bkahlert.nebula.viewer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.ViewerUtils;

/**
 * This extensions of the existing {@link org.eclipse.ui.dialogs.FilteredTree}
 * has two advantages:
 * <ol>
 * <li>The converter takes all columns into account. The original implementation
 * only considered the {@link TreeViewer}'s global {@link ILabelProvider}.</li>
 * <li>The expanded elements state is restored after search has finished. The
 * selected element stays also expanded.</li>
 * </ol>
 * 
 * @author bkahlert
 * 
 */
public class FilteredTree extends org.eclipse.ui.dialogs.FilteredTree {

	private static class PatternFilterWorkAround extends PatternFilter {
		// DIRTY this field is only kept here since we need it in the
		// construction of the viewer. This happens in the super constructor,
		// that's why there's no chance to save the reference before it is
		// actually used.
		private final TreeViewerFactory factory;

		public PatternFilterWorkAround(TreeViewerFactory factory) {
			this.factory = factory;
		}
	}

	/**
	 * Using a {@link FilteredTree} requires the {@link TreeViewer} to return an
	 * {@link ILabelProvider} for the whole thing. If {@link TreeViewerColumn}
	 * are used there is no single {@link ILabelProvider}. This
	 * {@link PatternFilter} considers all column's {@link ILabelProvider}s.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class GenericTreePatternFilter extends
			PatternFilterWorkAround {

		private final Map<Integer, CellLabelClient> cellLabelClients = new HashMap<Integer, CellLabelClient>();

		public GenericTreePatternFilter(TreeViewerFactory factory) {
			super(factory);
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

	/**
	 * This {@link PatternFilter} filters based on the value object behind each
	 * row.
	 * 
	 * @author bkahlert
	 * 
	 * @param <T>
	 */
	public static class URITreePatternFilter<T> extends PatternFilterWorkAround {

		private final IConverter<T, String> converter;

		public URITreePatternFilter(TreeViewerFactory factory,
				IConverter<T, String> converter) {
			super(factory);
			Assert.isNotNull(converter);
			this.converter = converter;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected boolean isLeafMatch(final Viewer viewer, final Object element) {
			if (!(viewer instanceof TreeViewer)) {
				return true;
			}

			try {
				String text = this.converter.convert((T) element);
				return this.wordMatches(text);
			} catch (ClassCastException e) {
				return true;
			}
		}
	}

	public static interface TreeViewerFactory {
		public TreeViewer create(Composite parent, int style);
	}

	private static final Logger LOGGER = Logger.getLogger(FilteredTree.class);

	/**
	 * Constructs a new instance that uses the provided converter to converter
	 * rows. The value object of each row is passed to the converter. If the
	 * converter returns true (or cannot be casted to <code>T</code>) the row is
	 * part of the result.
	 * 
	 * @author bkahlert
	 * 
	 */
	public <T> FilteredTree(Composite parent, int treeStyle,
			TreeViewerFactory factory, IConverter<T, String> filter) {
		super(parent, treeStyle, new URITreePatternFilter<T>(factory, filter),
				true);
	}

	/**
	 * Constructs a new instance that checks the text of all columns of each
	 * row.
	 * 
	 * @author bkahlert
	 * 
	 */
	public FilteredTree(Composite parent, int treeStyle,
			TreeViewerFactory factory) {
		super(parent, treeStyle, new GenericTreePatternFilter(factory), true);
	}

	@Override
	protected WorkbenchJob doCreateRefreshJob() {
		WorkbenchJob job = super.doCreateRefreshJob();
		job.addJobChangeListener(new JobChangeAdapter() {
			private TreePath[] expanded = null;

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				Job prefetcher = FilteredTree.this
						.prefetch(FilteredTree.this.treeViewer);

				final Semaphore mutex;
				if (prefetcher != null) {
					mutex = new Semaphore(0);
					prefetcher.addJobChangeListener(new JobChangeAdapter() {
						@Override
						public void done(IJobChangeEvent event) {
							mutex.release();
						}
					});
					prefetcher.schedule();
				} else {
					mutex = new Semaphore(1);
				}

				try {
					mutex.acquire();
				} catch (InterruptedException e1) {
					LOGGER.error("Error prefetching", e1);
				}
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
								TreeSelection selection = (TreeSelection) FilteredTree.this.treeViewer
										.getSelection();
								if (selection != null && selection.size() > 0) {
									expanded = ViewerUtils.addTreePath(
											expanded,
											ViewerUtils
													.createCompletedTreePaths(selection));
								}
								if (expanded != null
										&& (event.getResult() == Status.OK_STATUS)) {
									FilteredTree.this.treeViewer
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

	/**
	 * Can be overwritten if you need to preload data before the filtering takes
	 * place and blocks the UI thread.
	 * 
	 * @param treeViewer
	 * @return
	 */
	protected Job prefetch(TreeViewer treeViewer) {
		return null;
	}

	@Override
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		return ((PatternFilterWorkAround) this.getPatternFilter()).factory
				.create(parent, style);
	};

}
