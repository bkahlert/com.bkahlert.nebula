package com.bkahlert.nebula.widgets.image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ExecUtils;
import com.bkahlert.devel.nebula.widgets.browser.BrowserComposite;
import com.bkahlert.devel.nebula.widgets.timeline.TimelineJsonGenerator;
import com.bkahlert.nebula.browser.BrowserUtils;
import com.bkahlert.nebula.utils.ImageUtils;

/**
 * Shows an image in a way that it always fills the {@link Composite}'s
 * available width.
 * 
 * @author bkahlert
 * 
 */
public class Image extends BrowserComposite {

	/**
	 * Specifies the way the image should fill the canvas if the latter does not
	 * have the same proportions as the provided image.
	 * 
	 */
	public static enum FILL_MODE {
		/**
		 * Width or height does always match the corresponding width or height
		 * of the canvas. The other dimensions is at most as large the the
		 * canvas's one.
		 */
		INNER_FILL,

		/**
		 * The width of the image is always as big as the one of the canvas.
		 */
		HORIZONTAL,

		/**
		 * The height of the image is always as big as the one of the canvas.
		 */
		VERTICAL;
	}

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Image.class);

	public static Point extractSize(Object size) {
		if (size instanceof Object[] && ((Object[]) size).length == 2
				&& ((Object[]) size)[0] instanceof Double) {
			long width = Math.round((Double) ((Object[]) size)[0]);
			long height = Math.round((Double) ((Object[]) size)[1]);
			return new Point((int) width, (int) height);
		}
		return null;
	}

	public static interface IImageListener {
		public void imageLoaded(Point size);

		public void imageResized(Point size);
	}

	private final List<IImageListener> imageListeners = new ArrayList<IImageListener>();
	private final Object imageLoadMonitor = new Object();

	private FILL_MODE fillMode;

	private Point cachedOriginalSize;
	private Point cachedCurrentSize;

	public Image(Composite parent, int style, FILL_MODE fillMode) {
		this(parent, style, new Point(100, 100), fillMode);
	}

	public Image(Composite parent, int style, Point defaultSize,
			FILL_MODE fillMode) {
		super(parent, style);
		this.deactivateNativeMenu();

		this.fillMode = fillMode;

		this.cachedOriginalSize = defaultSize;
		this.cachedCurrentSize = defaultSize;

		new BrowserFunction(this.getBrowser(), "imageLoaded") {
			@Override
			public Object function(Object[] arguments) {
				if (arguments.length == 1) {
					Point originalSize = Image.extractSize(arguments[0]);
					Image.this.cachedOriginalSize = originalSize;
					Image.this.notifyImageLoaded();
					if (originalSize != null) {
						for (IImageListener imageListener : Image.this.imageListeners) {
							imageListener.imageLoaded(originalSize);
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
					Point currentSize = Image.extractSize(arguments[0]);
					Image.this.cachedCurrentSize = currentSize;
					if (currentSize != null) {
						for (IImageListener imageListener : Image.this.imageListeners) {
							imageListener.imageResized(currentSize);
						}
					}
				}
				return null;
			}
		};

		this.open(BrowserUtils.getFileUrl(Image.class, "html/index.html", "?internal=true"),
				5000);
	}

	@Override
	public void setBackground(Color color) {
		// TODO get rid of window.setTimeout
		String hex = new RGB(color.getRGB()).toHexString();
		this.run("window.setTimeout(function() {$('body').css('background-color', '"
				+ hex + "');},100);");
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
	public Future<Void> load(final String src, final Runnable callback) {
		return ExecUtils.nonUIAsyncExec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				String script = "com.bkahlert.nebula.image.load("
						+ TimelineJsonGenerator.enquote(src) + ", '"
						+ Image.this.fillMode.toString().toLowerCase() + "');";
				Future<Object> future = Image.this.run(script);
				Image.this.waitUntilImageLoaded();
				future.get();
				if (callback != null) {
					ExecUtils.asyncExec(new Runnable() {
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
	 * Loads an {@link org.eclipse.swt.graphics.Image} into the demoAreaContent
	 * area.
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
		Point size;
		if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
			size = this.getOriginalSize();
		} else if (wHint == SWT.DEFAULT && hHint != SWT.DEFAULT) {
			size = new Point(this.getWidth(hHint), hHint);
		} else if (wHint != SWT.DEFAULT && hHint == SWT.DEFAULT) {
			size = new Point(wHint, this.getHeight(wHint));
		} else {
			size = new Point(wHint, hHint);
		}
		Point maxSize = new Point(Math.min(this.getOriginalSize().x, size.x),
				Math.min(this.getOriginalSize().y, size.y));
		size = ImageUtils.resizeWithinArea(size, maxSize);
		return size;
	}

	public Point getOriginalSize() {
		return this.cachedOriginalSize;
	}

	public Point getCurrentSize() {
		return this.cachedCurrentSize;
	}

	public int getWidth(long height) {
		return (int) (this.cachedCurrentSize.x * ((double) height / this.cachedCurrentSize.y));
	}

	public int getHeight(long width) {
		return (int) (this.cachedCurrentSize.y * ((double) width / this.cachedCurrentSize.x));
	}

	public void limitToOriginalSize() {
		this.run("com.bkahlert.nebula.image.limitToOriginalSize();");
	}

	public void addImageListener(IImageListener imageListener) {
		this.imageListeners.add(imageListener);
	}

	public void removeImageListener(IImageListener imageListener) {
		this.imageListeners.remove(imageListener);
	}

}
