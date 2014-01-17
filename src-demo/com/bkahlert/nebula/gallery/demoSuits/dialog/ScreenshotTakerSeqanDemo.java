package com.bkahlert.nebula.gallery.demoSuits.dialog;

import java.io.File;
import java.net.URI;
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

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.gallery.annotations.Demo;
import com.bkahlert.nebula.gallery.demoSuits.AbstractDemo;
import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;
import com.bkahlert.nebula.screenshots.IScreenshotTaker.Format;
import com.bkahlert.nebula.screenshots.impl.webpage.FormContainingWebpageScreenshotRenderer;
import com.bkahlert.nebula.screenshots.impl.webpage.SingleFieldWebpage;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageBoundsFactory;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageBoundsFactory.Device;
import com.bkahlert.nebula.screenshots.impl.webpage.WebpageScreenshotTaker;
import com.bkahlert.nebula.widgets.image.Image;
import com.bkahlert.nebula.widgets.image.Image.FILL_MODE;

@Demo
public class ScreenshotTakerSeqanDemo extends AbstractDemo {

	public List<String> uris = Arrays
			.asList("http://docs.seqan.de/seqan/dev/INDEX_Page.html",
					"http://docs.seqan.de/seqan/dev2/panel/index.html",
					"http://trac.seqan.de/wiki/Tutorial/GettingStarted/WindowsVisualStudio",
					"https://trac.seqan.de/wiki/Tutorial");
	public List<String> queries = Arrays.asList("iter", "xyz");
	public List<Rectangle> bounds = Arrays.asList(WebpageBoundsFactory
			.getBounds(Device.IPHONE4, 0, 4965));

	// public BootstrapBrowser<String> uris = Arrays
	// .asList("http://docs.seqan.de/seqan/dev/INDEX_Page.html");
	// public BootstrapBrowser<String> queries = Arrays.asList("iter");
	// public BootstrapBrowser<Rectangle> bounds =
	// Arrays.asList(WebpageBoundsFactory
	// .getBounds(Device.IPHONE4, 0, 50));

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
					ExecutorUtil.nonUIAsyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								WebpageScreenshotTaker<SingleFieldWebpage> screenshotTaker = new WebpageScreenshotTaker<SingleFieldWebpage>(
										ScreenshotTakerSeqanDemo.this.numThreads,
										new FormContainingWebpageScreenshotRenderer<SingleFieldWebpage>(
												shell));

								final List<Future<File>> screenshots = new ArrayList<Future<File>>();
								for (String uri : ScreenshotTakerSeqanDemo.this.uris) {
									for (String query : ScreenshotTakerSeqanDemo.this.queries) {
										for (Rectangle bounds : ScreenshotTakerSeqanDemo.this.bounds) {
											Future<File> screenshot = screenshotTaker
													.takeScreenshot(
															new SingleFieldWebpage(
																	new URI(uri),
																	bounds,
																	3000,
																	new ISelector.FieldSelector(
																			"search"),
																	query, 500),
															Format.PNG);
											log("Submitted: query=" + query
													+ "; bounds="
													+ bounds.toString());
											screenshots.add(screenshot);
										}
									}
								}

								ExecutorUtil.syncExec(new Runnable() {
									@Override
									public void run() {
										CompositeUtils
												.emptyComposite(ScreenshotTakerSeqanDemo.this.parent);
										ScreenshotTakerSeqanDemo.this.parent
												.setLayout(new GridLayout(
														screenshots.size() / 3,
														false));
									}
								});

								for (Future<File> screenshot : screenshots) {
									try {
										final File file = screenshot.get();
										ExecutorUtil.asyncExec(new Runnable() {
											@Override
											public void run() {
												Image image = new Image(
														ScreenshotTakerSeqanDemo.this.parent,
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
									} catch (Exception e) {
										log("Screenshot failed");
									}
								}

								screenshotTaker.dispose();
								ExecutorUtil.syncExec(new Runnable() {
									@Override
									public void run() {
										ScreenshotTakerSeqanDemo.this.parent
												.layout();
									}
								});

							} catch (Exception e) {
								log(e.getMessage());
							}
						}
					});
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
					ScreenshotTakerSeqanDemo.this.numThreads = Integer
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
