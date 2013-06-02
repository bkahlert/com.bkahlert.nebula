package com.bkahlert.nebula.gallery.demoSuits.dialog;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.screenshots.ScreenshotInfo;
import com.bkahlert.nebula.screenshots.ScreenshotTaker;
import com.bkahlert.nebula.widgets.image.Image;

@Demo
public class ScreenshotTakerDemo extends AbstractDemo {

	private Image image = null;

	@Override
	public void createControls(final Composite composite) {
		Button openPage = new Button(composite, SWT.PUSH);
		openPage.setText("Open Page");
		openPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final ScreenshotTaker screenshotTaker = new ScreenshotTaker();
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								final File file = screenshotTaker
										.takeScreenshot(
												new ScreenshotInfo(
														"http://validator.w3.org",
														new Point(50, 100),
														new Point(600, 400)),
												"png", 3000, null).get();
								log("Screenshot taken: "
										+ file.getAbsolutePath());
								ScreenshotTakerDemo.this.image.load(
										file.getAbsolutePath(), new Runnable() {
											@Override
											public void run() {
												log("Screenshot has been loaded: "
														+ file.getAbsolutePath());
											}
										});
							} catch (Exception e) {
								log(e.getMessage());
							}
						}
					}).start();
				} catch (Exception e1) {
					log(e1.getMessage());
				}
			}
		});
	}

	@Override
	public void createDemo(Composite composite) {
		composite.setLayout(new FillLayout());
		this.image = new Image(composite, SWT.NONE);
	}

}
