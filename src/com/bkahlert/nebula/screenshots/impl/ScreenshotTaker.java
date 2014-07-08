package com.bkahlert.nebula.screenshots.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer.IScreenshotRendererSession;
import com.bkahlert.nebula.screenshots.IScreenshotTaker;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.ShellUtils;

public class ScreenshotTaker<SUBJECT> implements IScreenshotTaker<SUBJECT> {

	private static final Logger LOGGER = Logger
			.getLogger(ScreenshotTaker.class);

	private ExecutorService queue;
	private IScreenshotRenderer<SUBJECT, ?> renderer;

	public ScreenshotTaker(int numThreads,
			IScreenshotRenderer<SUBJECT, ?> renderer) {
		this.queue = Executors.newFixedThreadPool(numThreads,
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r, ScreenshotTaker.class
								.getSimpleName());
						return thread;
					}
				});
		this.renderer = renderer;
	}

	@Override
	public Future<File> takeScreenshot(final SUBJECT subject,
			final Format format) {
		return this.queue.submit(new Callable<File>() {
			@Override
			public File call() throws Exception {
				final IScreenshotRendererSession session = ScreenshotTaker.this.renderer
						.render(subject).call();

				Image image;
				synchronized (Display.getDefault()) {
					Control control = session.display();
					LOGGER.info("Capturing rendered " + subject);
					image = ShellUtils.captureScreen(control);
					session.dispose();
				}

				File file = ImageUtils.saveImageToTempFile(image,
						format.getName());
				image.dispose();
				return file;
			}
		});
	}

	@Override
	public List<Future<File>> takeScreenshots(List<SUBJECT> subjects,
			Format format) {
		List<Future<File>> screenshots = new ArrayList<Future<File>>();
		for (SUBJECT request : subjects) {
			screenshots.add(this.takeScreenshot(request, Format.PNG));
		}
		return screenshots;
	}

	@Override
	public void dispose() {
		if (this.renderer != null) {
			this.renderer.dispose();
		}
	}

}
