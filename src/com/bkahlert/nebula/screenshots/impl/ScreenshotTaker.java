package com.bkahlert.nebula.screenshots.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer.IScreenshotRendererSession;
import com.bkahlert.nebula.screenshots.IScreenshotRequest;
import com.bkahlert.nebula.screenshots.IScreenshotTaker;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.ShellUtils;

public class ScreenshotTaker<REQUEST extends IScreenshotRequest> implements
		IScreenshotTaker<REQUEST> {

	private static final Logger LOGGER = Logger
			.getLogger(ScreenshotTaker.class);

	private ExecutorService queue;
	private IScreenshotRenderer<REQUEST> renderer;

	public ScreenshotTaker(int numThreads, IScreenshotRenderer<REQUEST> renderer) {
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
	public Future<File> submitOrder(final REQUEST request) {
		return this.queue.submit(new Callable<File>() {
			@Override
			public File call() throws Exception {
				final IScreenshotRendererSession session = ScreenshotTaker.this.renderer
						.render(request).call();

				BufferedImage image;
				synchronized (Display.getDefault()) {
					session.bringToFront();
					Rectangle bounds = session.getBounds();
					LOGGER.info("Capturing " + bounds);
					image = ShellUtils.captureScreen(bounds);
					session.dispose();
				}

				return ImageUtils.saveImageToTempFile(image, request
						.getFormat().getName());
			}
		});
	}

	@Override
	public List<Future<File>> submitOrder(List<REQUEST> requests) {
		List<Future<File>> screenshots = new ArrayList<Future<File>>();
		for (REQUEST request : requests) {
			screenshots.add(this.submitOrder(request));
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
