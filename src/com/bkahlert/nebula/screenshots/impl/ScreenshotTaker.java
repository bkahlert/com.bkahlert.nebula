package com.bkahlert.nebula.screenshots.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.eclipse.swt.widgets.Display;

import com.bkahlert.nebula.screenshots.IScreenshotRenderer;
import com.bkahlert.nebula.screenshots.IScreenshotRenderer.IScreenshotRendererSession;
import com.bkahlert.nebula.screenshots.IScreenshotRequest;
import com.bkahlert.nebula.screenshots.IScreenshotTaker;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.ShellUtils;

public class ScreenshotTaker<ORDER extends IScreenshotRequest> implements
		IScreenshotTaker<ORDER> {

	private ExecutorService queue;
	private IScreenshotRenderer<ORDER> renderer;

	public ScreenshotTaker(int numThreads, IScreenshotRenderer<ORDER> renderer) {
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
	public Future<File> submitOrder(final ORDER order) {
		return this.queue.submit(new Callable<File>() {
			@Override
			public File call() throws Exception {
				final IScreenshotRendererSession session = ScreenshotTaker.this.renderer
						.render(order).call();

				BufferedImage image;
				synchronized (Display.getDefault()) {
					session.bringToFront();
					image = ShellUtils.captureScreen(session.getBounds());
					session.dispose();
				}

				return ImageUtils.saveImageToTempFile(image, order.getFormat()
						.getName());
			}
		});
	}

	@Override
	public void dispose() {
		if (this.renderer != null) {
			this.renderer.dispose();
		}
	}

}
