package com.bkahlert.nebula.gallery.demoSuits.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.utils.colors.ColorUtils;
import com.bkahlert.nebula.widgets.composer.ComposerReadOnly;

@Demo
public class ComposerReadOnlyDemo extends AbstractDemo {

	private ComposerReadOnly composer = null;

	@Override
	public void createDemo(Composite composite) {
		this.composer = new ComposerReadOnly(composite, SWT.NONE);
		this.composer.setSource("Hello <b>CKEditor</b>!");
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
		Button composerRandomBackgroundColor = new Button(composite, SWT.PUSH);
		composerRandomBackgroundColor.setText("Random Background Color");

		composerGetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					ComposerReadOnlyDemo.log(ComposerReadOnlyDemo.this.composer
							.getSource().get());
				} catch (Exception ex) {
					log(ex);
				}
			}
		});
		composerSetSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerReadOnlyDemo.this.composer
						.setSource("<p title='test'><b>Hallo</b><i>Welt!</i></p>");
			}
		});
		composerShowSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerReadOnlyDemo.this.composer.showSource();
			}
		});
		composerHideSource.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ComposerReadOnlyDemo.this.composer.hideSource();
			}
		});
		composerRandomBackgroundColor
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						ComposerReadOnlyDemo.this.composer
								.setBackground(new Color(Display.getCurrent(),
										ColorUtils.getRandomRGB()
												.toClassicRGB()));
					}
				});

	}

}
