package com.bkahlert.nebula.viewer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

import com.bkahlert.nebula.utils.CellLabelClient;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.IConverter;
import com.bkahlert.nebula.utils.Stylers;
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
				CellLabelClient.INSTANCE.setElement(cellLabelProvider, element);
				String labelText = CellLabelClient.INSTANCE.getText();
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
			private String lastFilterString = "";
			private TreePath[] expanded = null;
			private Map<TreeViewerColumn, CellLabelProvider> labelProviders = null;

			@Override
			public void aboutToRun(IJobChangeEvent event) {
				boolean filterStarted;
				{
					String s = FilteredTree.this.getFilterString();
					filterStarted = !s.equals(this.lastFilterString)
							&& !s.isEmpty();
					this.lastFilterString = s;
				}

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

				if (filterStarted) {
					try {
						ExecUtils.syncExec(() -> {
							if (!FilteredTree.this.getFilterString().isEmpty()
									&& this.expanded == null) {
								this.expanded = FilteredTree.this.getViewer()
										.getExpandedTreePaths();
							}
						});
					} catch (Exception e) {
						this.expanded = null;
						LOGGER.error("Error saving expanded state of "
								+ FilteredTree.this.getViewer());
					}

					try {
						ExecUtils.syncExec(() -> {
							this.labelProviders = new HashMap<>();
							for (TreeViewerColumn column : ViewerUtils
									.getColumns(FilteredTree.this.getViewer())) {

								CellLabelProvider labelProvider = ViewerUtils
										.getLabelProvider(column);
								if (labelProvider != null) {
									this.labelProviders.put(column,
											labelProvider);
									column.setLabelProvider(new DelegatingStyledCellLabelProvider(
											new StyledLabelProvider() {
												@Override
												public StyledString getStyledText(
														Object element) {
													CellLabelClient.INSTANCE
															.setElement(
																	labelProvider,
																	element);
													StyledString s = CellLabelClient.INSTANCE
															.getStyledText();
													return Stylers
															.apply(s,
																	Stylers.BOLD_STYLER,
																	Arrays.asList(FilteredTree.this
																			.getWords()));
												}

												@Override
												public Image getImage(
														Object element) {
													CellLabelClient.INSTANCE
															.setElement(
																	labelProvider,
																	element);
													return CellLabelClient.INSTANCE
															.getImage();
												}
											}));
								}
							}
						});
					} catch (Exception e) {
						this.labelProviders = null;
						LOGGER.error("Error highlighting filtering text in "
								+ FilteredTree.this.getViewer());
					}
				}
				super.aboutToRun(event);
			}

			@Override
			public void done(final IJobChangeEvent event) {
				super.done(event);
				try {
					ExecUtils
							.syncExec(() -> {
								if (FilteredTree.this.getFilterString()
										.isEmpty()) {
									TreeSelection selection = (TreeSelection) FilteredTree.this.treeViewer
											.getSelection();
									if (selection != null
											&& selection.size() > 0) {
										this.expanded = ViewerUtils
												.addTreePath(
														this.expanded,
														ViewerUtils
																.createCompletedTreePaths(selection));
									}
									if (this.expanded != null
											&& (event.getResult() == Status.OK_STATUS)) {
										FilteredTree.this.treeViewer
												.setExpandedTreePaths(this.expanded);
										this.expanded = null;
									}

									if (this.labelProviders != null) {
										try {
											ExecUtils
													.syncExec(() -> {
														for (TreeViewerColumn column : this.labelProviders
																.keySet()) {
															column.setLabelProvider(this.labelProviders
																	.get(column));
														}
													});
										} catch (Exception e) {
											LOGGER.error("Error highlighting filtering text in "
													+ FilteredTree.this
															.getViewer());
										}
										this.labelProviders = null;
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

	@Override
	protected String getFilterString() {
		try {
			String s = ExecUtils.syncExec(() -> FilteredTree.super
					.getFilterString());
			if (s == null || s.equals(this.initialText)) {
				return "";
			}
			return s;
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return "";
	}

	private String[] getWords() {
		try {
			Method m = PatternFilter.class.getDeclaredMethod("getWords",
					String.class);
			m.setAccessible(true);
			String[] words = (String[]) m.invoke(this.getPatternFilter(),
					this.getFilterString());
			m.setAccessible(false);
			return words;
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			LOGGER.error(e);
		}
		return this.getFilterString().split("\\s+");
	}
}
