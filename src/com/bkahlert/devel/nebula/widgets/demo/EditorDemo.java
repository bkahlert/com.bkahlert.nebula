package com.bkahlert.devel.nebula.widgets.demo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.composer.Composer.ToolbarSet;
import com.bkahlert.devel.nebula.widgets.editor.AutosaveEditor;
import com.bkahlert.devel.nebula.widgets.editor.Editor;

public class EditorDemo extends Composite {

	public EditorDemo(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite composerControls = new RoundedComposite(this, SWT.BORDER);
		composerControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		composerControls.setLayout(new RowLayout());

		Button composerSetSource = new Button(composerControls, SWT.PUSH);
		composerSetSource.setText("Set Source 1");
		Button composerSetSource2 = new Button(composerControls, SWT.PUSH);
		composerSetSource2.setText("Set Source 2");

		final Editor<String> editor = new AutosaveEditor<String>(this,
				SWT.NONE, 500, ToolbarSet.DEFAULT) {
			@Override
			public String getHtml(String objectToLoad, IProgressMonitor monitor) {
				return objectToLoad;
			}

			@Override
			public void setHtml(String objectToLoad, String html,
					IProgressMonitor monitor) {
				System.out.println("saved: " + html);
			}
		};
		try {
			editor.load("This is an auto-saving editor");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor.load("Hello CKEditor - Editor!");

		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.load("<p title='test'><b>Hallo</b><i>Welt 1!</i></p>");
			}
		});
		composerSetSource2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editor.load("<p title='test'><b>Hallo</b><i>Welt 2!</i></p>");
			}
		});
	}

}
