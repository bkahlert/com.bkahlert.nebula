package com.bkahlert.devel.nebula.widgets.demo;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.widgets.RoundedComposite;
import com.bkahlert.devel.nebula.widgets.browser.IAnker;
import com.bkahlert.devel.nebula.widgets.browser.IJavaScriptExceptionListener;
import com.bkahlert.devel.nebula.widgets.browser.JavaScriptException;
import com.bkahlert.devel.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.devel.nebula.widgets.composer.Composer;
import com.bkahlert.devel.nebula.widgets.composer.IAnkerLabelProvider;

public class ComposerDemo extends Composite {

	public ComposerDemo(Composite parent, int style) {
		super(parent, style);

		this.setLayout(GridLayoutFactory.fillDefaults().create());

		Composite composerControls = new RoundedComposite(this, SWT.BORDER);
		composerControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		composerControls.setLayout(new RowLayout());

		Button composerGetSource = new Button(composerControls, SWT.PUSH);
		composerGetSource.setText("Get Source");
		Button composerSetSource = new Button(composerControls, SWT.PUSH);
		composerSetSource.setText("Set Source");
		Button composerShowSource = new Button(composerControls, SWT.PUSH);
		composerShowSource.setText("Show Source");
		Button composerHideSource = new Button(composerControls, SWT.PUSH);
		composerHideSource.setText("Hide Source");
		Button composerSelectAll = new Button(composerControls, SWT.PUSH);
		composerSelectAll.setText("Select All");
		Button composerEnable = new Button(composerControls, SWT.PUSH);
		composerEnable.setText("Enable");
		Button composerDisable = new Button(composerControls, SWT.PUSH);
		composerDisable.setText("Disable");
		Button composerLockSelection = new Button(composerControls, SWT.PUSH);
		composerLockSelection.setText("Save Selection");
		Button composerUnlockSelection = new Button(composerControls, SWT.PUSH);
		composerUnlockSelection.setText("Restore Selection");

		final Composer composer = new Composer(this, SWT.BORDER);
		composer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composer.setSource("Hello CKEditor - Composer");
		composer.addJavaScriptExceptionListener(new IJavaScriptExceptionListener() {
			@Override
			public boolean thrown(JavaScriptException exception) {
				System.err.println(exception);
				return true;
			}
		});
		composer.addAnkerLabelProvider(new IAnkerLabelProvider() {
			@Override
			public boolean isResponsible(IAnker anker) {
				return anker.getContent().contains("test");
			}

			@Override
			public String getHref(IAnker anker) {
				return "http://bkahlert.com";
			}

			@Override
			public String[] getClasses(IAnker anker) {
				return new String[] { "special" };
			}

			@Override
			public String getContent(IAnker anker) {
				return "Link to bkahlert.com";
			}
		});
		composer.addAnkerListener(new IAnkerListener() {
			@Override
			public void ankerClicked(IAnker anker, boolean special) {
				System.err.println((special ? "special " : "") + "clicked on "
						+ anker.getHref());
			}
		});
		composer.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				System.err.println("changed: " + e.data);
			}
		});

		composerGetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(composer.getSource());
			}
		});
		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.setSource("<p title='test'><b>Hallo</b><i>Welt!</i></p>");
			}
		});
		composerShowSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.showSource();
			}
		});
		composerHideSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.hideSource();
			}
		});
		composerSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.selectAll();
			}
		});
		composerEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.setEnabled(true);
			}
		});
		composerDisable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.setEnabled(false);
			}
		});
		composerLockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.saveSelection();
			}
		});
		composerUnlockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				composer.restoreSelection();
			}
		});
	}

}
