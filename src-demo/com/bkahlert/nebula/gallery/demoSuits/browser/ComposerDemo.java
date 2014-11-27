package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bkahlert.nebula.dialogs.PopupDialog;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.widgets.browser.extended.html.IAnker;
import com.bkahlert.nebula.widgets.browser.listener.IAnkerListener;
import com.bkahlert.nebula.widgets.composer.Composer;
import com.bkahlert.nebula.widgets.composer.IAnkerLabelProvider;

@Demo
public class ComposerDemo extends AbstractDemo {

	private Composer composer = null;

	@Override
	public void createDemo(Composite composite) {
		this.composer = new Composer(composite, SWT.BORDER);
		this.composer.setSource("Hello <b>CKEditor</b>!");
		this.composer.setTitle("Hello CKEditor!");
		this.composer.addAnkerLabelProvider(new IAnkerLabelProvider() {
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
		this.composer.addAnkerListener(new IAnkerListener() {
			private PopupDialog popup = null;

			@Override
			public void ankerClicked(IAnker anker) {
				AbstractDemo.log("clicked on " + anker.getHref());
			}

			@Override
			public void ankerHovered(final IAnker anker, boolean entered) {
				AbstractDemo.log((entered ? "entered " : "left") + ": "
						+ anker.getHref());
				if (this.popup != null) {
					this.popup.close();
					this.popup = null;
				}
				if (entered) {
					this.popup = new PopupDialog() {
						@Override
						protected Control createControls(Composite parent) {
							Label label = new Label(parent, SWT.NONE);
							label.setText(anker.toHtml());
							return label;
						};
					};
					this.popup.open();
				}
			}
		});
		this.composer.addModifyListener(e -> AbstractDemo.log("changed: "
				+ e.data));
	}

	@Override
	public void createControls(Composite composite) {
		Button composerGetSource = new Button(composite, SWT.PUSH);
		composerGetSource.setText("Get Source");
		Button composerSetSource = new Button(composite, SWT.PUSH);
		composerSetSource.setText("Set Source");
		Button composerShowSource = new Button(composite, SWT.PUSH);
		composerShowSource.setText("Show Source");
		Button composerHideSource = new Button(composite, SWT.PUSH);
		composerHideSource.setText("Hide Source");
		Button composerSelectAll = new Button(composite, SWT.PUSH);
		composerSelectAll.setText("Select All");
		Button composerEnable = new Button(composite, SWT.PUSH);
		composerEnable.setText("Enable");
		Button composerDisable = new Button(composite, SWT.PUSH);
		composerDisable.setText("Disable");
		Button composerLockSelection = new Button(composite, SWT.PUSH);
		composerLockSelection.setText("Save Selection");
		Button composerUnlockSelection = new Button(composite, SWT.PUSH);
		composerUnlockSelection.setText("Restore Selection");

		composerGetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					AbstractDemo.log(ComposerDemo.this.composer.getSource()
							.get());
				} catch (Exception ex) {
					log(ex);
				}
			}
		});
		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer
						.setSource("<p title='test'><b>Hallo</b><i>Welt!</i></p>");
			}
		});
		composerShowSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.showSource();
			}
		});
		composerHideSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.hideSource();
			}
		});
		composerSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.selectAll();
			}
		});
		composerEnable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.setEnabled(true);
			}
		});
		composerDisable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.setEnabled(false);
			}
		});
		composerLockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.saveSelection();
			}
		});
		composerUnlockSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerDemo.this.composer.restoreSelection();
			}
		});
	}

}
