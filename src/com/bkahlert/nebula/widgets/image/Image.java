package com.bkahlert.nebula.widgets.image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.utils.ImageUtils;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.IJavaScriptExceptionListener;
import com.bkahlert.devel.nebula.widgets.browser.JavaScriptException;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;

/**
 * Shows an image in a way that it always fills the {@link Composite}'s
 * available width.
 * 
 * @author bkahlert
 * 
 */
public class Image extends BrowserComposite {

	private static final Logger LOGGER = Logger.getLogger(Image.class);

	public static long[] extractSize(Object size) {
		if (size instanceof Object[] && ((Object[]) size).length == 2
				&& ((Object[]) size)[0] instanceof Double) {
			long width = Math.round((Double) ((Object[]) size)[0]);
			long height = Math.round((Double) ((Object[]) size)[1]);
			return new long[] { width, height };
		}
		return null;
	}

	public static interface IImageListener {
		public void imageLoaded(long width, long height);

		public void imageResized(long width, long height);
	}

	private List<IImageListener> imageListeners = new ArrayList<IImageListener>();
	private final Object imageLoadMonitor = new Object();
	private long[] cachedOriginalSize = new long[] { 100, 100 };
	private long[] cachedCurrentSize = new long[] { 100, 100 };

	public Image(Composite parent, int style) {
		super(parent, style, getFileUrl(Image.class, "html/index.html")
				+ "?internal=true");
		this.deactivateNativeMenu();

		this.addJavaScriptExceptionListener(new IJavaScriptExceptionListener() {
			@Override
			public boolean thrown(JavaScriptException e) {
				LOGGER.error("Internal " + Image.class.getSimpleName()
						+ " error", e);
				return true;
			}
		});

		new BrowserFunction(this.getBrowser(), "imageLoaded") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					long[] originalSize = Image.extractSize(arguments[0]);
					Image.this.cachedOriginalSize = originalSize;
					Image.this.notifyImageLoaded();
					if (originalSize != null) {
						for (IImageListener imageListener : Image.this.imageListeners) {
							imageListener.imageLoaded(originalSize[0],
									originalSize[1]);
						}
					}
				}
				return null;
			}
		};

		new BrowserFunction(this.getBrowser(), "imageResized") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					long[] currentSize = Image.extractSize(arguments[0]);
					Image.this.cachedCurrentSize = currentSize;
					if (currentSize != null) {
						for (IImageListener imageListener : Image.this.imageListeners) {
							imageListener.imageResized(currentSize[0],
									currentSize[1]);
						}
					}
				}
				return null;
			}
		};
	}

	protected void notifyImageLoaded() {
		synchronized (Image.this.imageLoadMonitor) {
			Image.this.imageLoadMonitor.notifyAll();
		}
	}

	protected void waitUntilImageLoaded() throws InterruptedException {
		synchronized (Image.this.imageLoadMonitor) {
			Image.this.imageLoadMonitor.wait();
		}
	}

	/**
	 * Loads the given source and calls the optional {@link Runnable} if the
	 * source has been loaded.
	 * 
	 * @param src
	 * @param callback
	 *            is called in the UI thread when the source has been loaded.
	 */
	public void load(final String src, final Runnable callback) {
		ExecutorUtil.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				String script = "com.bkahlert.nebula.image.load("
						+ TimelineJsonGenerator.enquote(src) + ");";
				Future<Object> future = Image.this.run(script);
				Image.this.waitUntilImageLoaded();
				future.get();
				if (callback != null) {
					ExecutorUtil.syncExec(new Runnable() {
						@Override
						public void run() {
							callback.run();
						}
					});
				}
				return null;
			}
		});
	}

	/**
	 * Loads an {@link org.eclipse.swt.graphics.Image} into the content area.
	 * <p>
	 * The {@link org.eclipse.swt.graphics.Image} may directly be disposed after
	 * having called this method.
	 * 
	 * @param image
	 * @param callback
	 *            is called in the UI thread when the
	 *            {@link org.eclipse.swt.graphics.Image} has been loaded.
	 */
	public void load(org.eclipse.swt.graphics.Image image, Runnable callback) {
		String base64 = ImageUtils.convertToInlineSrc(image);
		this.load(base64, callback);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
			long[] size = this.getOriginalSize();
			return new Point((int) size[0], (int) size[1]);
		} else if (wHint == SWT.DEFAULT && hHint != SWT.DEFAULT) {
			long width = this.getWidth(hHint);
			return new Point((int) width, hHint);
		} else if (wHint != SWT.DEFAULT && hHint == SWT.DEFAULT) {
			long height = this.getHeight(wHint);
			return new Point(wHint, (int) height);
		} else {
			return new Point(wHint, hHint);
		}
	}

	public long[] getOriginalSize() {
		return this.cachedOriginalSize;
	}

	public long[] getCurrentSize() {
		return this.cachedCurrentSize;
	}

	public long getWidth(long height) {
		return (long) (this.cachedCurrentSize[0] * ((double) height / this.cachedCurrentSize[1]));
	}

	public long getHeight(long width) {
		return (long) (this.cachedCurrentSize[1] * ((double) width / this.cachedCurrentSize[0]));
	}

	public void addImageListener(IImageListener imageListener) {
		this.imageListeners.add(imageListener);
	}

	public void removeImageListener(IImageListener imageListener) {
		this.imageListeners.remove(imageListener);
	}

}
