package com.bkahlert.nebula.viewer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class FilterableTreeViewer extends SortableTreeViewer {

	private final ViewerFilter filter = new ViewerFilter() {
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			IBaseLabelProvider labelProvider = FilterableTreeViewer.this
					.getLabelProvider();

		}
	};

	private final Text text;

	public FilterableTreeViewer(Composite parent, int style) {
		super(new Composite(parent, SWT.NONE), style);
		Composite composite = this.getControl().getParent();
		composite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		Tree tree = this.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.text = new Text(composite, style);
		this.text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.text.setText("");
		this.text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				System.err.println(e);
			}
		});

		this.addFilter(this.filter);
	}

}
