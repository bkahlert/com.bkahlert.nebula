package com.bkahlert.nebula.gallery.demoSuits.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;
import com.bkahlert.nebula.screenshots.IScreenshotTaker.Format;
import com.bkahlert.nebula.screenshots.impl.webpage.FormContainingWebpageScreenshotRenderer;
import com.bkahlert.nebula.screenshots.impl.webpage.GoogleWebpage;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageBoundsFactory;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageBoundsFactory.Device;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotTaker;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.image.Image;
import com.bkahlert.nebula.widgets.image.Image.FILL_MODE;

@Demo
public class ScreenshotTakerDemo extends AbstractDemo {

	public List<String> queries = Arrays.asList("SeqAn", "Saros");
	public List<Rectangle> bounds = Arrays.asList(
			WebpageBoundsFactory.getBounds(Device.IPHONE3, 100, 100),
			WebpageBoundsFactory.getBounds(Device.IPHONE4, 0, 50),
			WebpageBoundsFactory.getBounds(Device.IPHONE5, 0, 0),
			WebpageBoundsFactory.getBounds(Device.IPAD, 0, 0),
			WebpageBoundsFactory.getBounds(Device.NETBOOK, 200, 200),
			WebpageBoundsFactory.getBounds(Device.LAPTOP15, 0, 0),
			new Rectangle(100, 100, 5000, 4000));

	private int numThreads = 4;
	private Composite parent;

	@Override
	public void createControls(final Composite composite) {
		Button openPage = new Button(composite, SWT.PUSH);
		openPage.setText("Generate Screenshot!");
		openPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Shell shell = composite.getShell();
				try {
					ExecUtils.nonUISyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								WebpageScreenshotTaker<GoogleWebpage> screenshotTaker = new WebpageScreenshotTaker<GoogleWebpage>(
										ScreenshotTakerDemo.this.numThreads,
										new FormContainingWebpageScreenshotRenderer<GoogleWebpage>(
												shell));

								final List<Future<File>> screenshots = new ArrayList<Future<File>>();
								for (String query : ScreenshotTakerDemo.this.queries) {
									for (Rectangle bounds : ScreenshotTakerDemo.this.bounds) {
										Future<File> screenshot = screenshotTaker
												.takeScreenshot(
														new GoogleWebpage(
																bounds, 3000,
																query),
														Format.PNG);
										log("Submitted: query=" + query
												+ "; bounds="
												+ bounds.toString());
										screenshots.add(screenshot);
									}
								}

								ExecUtils.syncExec(new Runnable() {
									@Override
									public void run() {
										CompositeUtils
												.emptyComposite(ScreenshotTakerDemo.this.parent);
										ScreenshotTakerDemo.this.parent
												.setLayout(new GridLayout(
														screenshots.size() / 3,
														false));
									}
								});

								for (Future<File> screenshot : screenshots) {
									final File file = screenshot.get();
									ExecUtils.asyncExec(new Runnable() {
										@Override
										public void run() {
											Image image = new Image(
													ScreenshotTakerDemo.this.parent,
													SWT.BORDER,
													FILL_MODE.INNER_FILL);
											image.setLayoutData(new GridData(
													SWT.CENTER, SWT.CENTER,
													true, true));
											image.load(
													"file://"
															+ file.getAbsolutePath(),
													null);
											composite.layout();
										}
									});

									log("Screenshot created: " + file);
								}

								screenshotTaker.dispose();
								ExecUtils.syncExec(new Runnable() {
									@Override
									public void run() {
										ScreenshotTakerDemo.this.parent
												.layout();
									}
								});

							} catch (Exception e) {
								log(e.getMessage());
							}
						}
					});

					// log(fileFuture.get().toString());

					// final ScreenshotTaker screenshotTaker = new
					// ScreenshotTaker();
					// new Thread(new Runnable() {
					// @Override
					// public void run() {
					// try {
					// final File file = screenshotTaker
					// .takeScreenshot(
					// new ScreenshotInfo(
					// "http://validator.w3.org",
					// new Point(50, 100),
					// new Point(600, 400)),
					// "png", 3000, null).get();
					// log("Screenshot taken: "
					// + file.getAbsolutePath());
					// ScreenshotTakerDemo.this.image.load(
					// file.getAbsolutePath(), new Runnable() {
					// @Override
					// public void run() {
					// log("Screenshot has been loaded: "
					// + file.getAbsolutePath());
					// }
					// });
					// } catch (Exception e) {
					// log(e.getMessage());
					// }
					// }
					// }).start();
				} catch (Exception e1) {
					log(e1.getMessage());
				}
			}
		});

		final Text numThreadsText = new Text(composite, SWT.BORDER);
		numThreadsText.setText(this.numThreads + "");
		numThreadsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					ScreenshotTakerDemo.this.numThreads = Integer
							.parseInt(numThreadsText.getText());
				} catch (Exception e1) {
					log(e1.getMessage());
				}
			}
		});
	}

	@Override
	public void createDemo(Composite composite) {
		this.parent = composite;
	}

}
