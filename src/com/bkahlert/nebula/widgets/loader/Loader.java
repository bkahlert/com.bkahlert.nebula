package com.bkahlert.nebula.widgets.loader;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.MathUtils;
import com.bkahlert.nebula.widgets.browser.Browser;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class Loader {

	private static final Logger LOGGER = Logger.getLogger(Loader.class);

	private static final Color SPINNER_BACKGROUND = Display.getCurrent()
			.getSystemColor(SWT.COLOR_BLACK);
	private static final int SPINNER_BACKGROUND_ALPHA = 127;
	private static final File SPINNER_FILE = BrowserUtils.getFile(Loader.class,
			"spinner.png");
	private static final Image SPINNER = new Image(Display.getCurrent(),
			SPINNER_FILE.getAbsolutePath());
	static final int NUM_SPINNER_SPRITES = 12;

	private static Dimension SPINNER_DIMENSIONS;
	static {
		try {
			SPINNER_DIMENSIONS = ImageUtils.getImageDimensions(SPINNER_FILE);
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	/**
	 * This timer is used to schedule {@link #redrawSpinnerTimerTask}.
	 */
	private Timer timer = null;

	/**
	 * The {@link TimerTask}
	 */
	private final TimerTask redrawSpinnerTimerTask = new TimerTask() {
		@Override
		public void run() {
			try {
				ExecUtils.syncExec(new Runnable() {
					@Override
					public void run() {
						if (Loader.this.control != null
								&& !Loader.this.control.isDisposed()) {
							Loader.this.control.redraw();
						}
					}
				});
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
	};

	/**
	 * The {@link Control} that this {@link Loader} is decorating.
	 */
	private final Control control;

	/**
	 * This {@link PaintListener} is responsible to draw the spinner.
	 */
	private final PaintListener spinnerDrawingPaintListener = new PaintListener() {
		private int sprite = 0;

		@Override
		public void paintControl(PaintEvent e) {
			Point controlSize = Loader.this.control.getSize();
			int size = MathUtils.min(SPINNER_DIMENSIONS.height, controlSize.x,
					controlSize.y) / 2;
			e.gc.setClipping(Loader.this.control.getRegion());
			e.gc.setAlpha(SPINNER_BACKGROUND_ALPHA);
			e.gc.setBackground(SPINNER_BACKGROUND);
			e.gc.fillRectangle(new Rectangle(0, 0, controlSize.x, controlSize.y));
			e.gc.setAlpha(255);
			e.gc.drawImage(SPINNER, SPINNER_DIMENSIONS.height * this.sprite, 0,
					SPINNER_DIMENSIONS.height, SPINNER_DIMENSIONS.height,
					(controlSize.x - size) / 2, (controlSize.y - size) / 2,
					size, size);
			this.sprite++;
			if (this.sprite >= NUM_SPINNER_SPRITES) {
				this.sprite = 0;
			}
		}
	};

	/**
	 * Due to a bug in SWT it is not possible to paint on a
	 * {@link org.eclipse.swt.browser.Browser}. A shell containing a
	 * {@link Loader} decorated {@link Composite} is used to overlay the
	 * {@link org.eclipse.swt.browser.Browser}.
	 */
	private Shell shell;

	/**
	 * The {@link Loader} that is used within the {@link #shell}.
	 */
	private Loader loader;

	public Loader(Control control) {
		super();
		Assert.isNotNull(control);
		this.control = control;
		this.control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				Loader.this.dispose();
			}
		});
	}

	public void start() {
		this.stop();
		if (this.control instanceof Browser
				|| this.control instanceof org.eclipse.swt.browser.Browser) {
			Loader.this.shell = new Shell(this.control.getDisplay(),
					SWT.NO_TRIM | SWT.ON_TOP);
			Composite composite = new Composite(this.shell, SWT.NONE);
			this.shell.setLayout(new FillLayout());
			this.loader = new Loader(composite);
			this.loader.start();
			this.shell.setSize(this.control.getSize());
			this.shell.setLocation(this.control.toDisplay(0, 0));
			this.shell.setRegion(this.control.getRegion());
			this.shell.open();
		} else {
			this.control.addPaintListener(this.spinnerDrawingPaintListener);
			this.control.redraw();
			this.timer = new Timer();
			this.timer.scheduleAtFixedRate(this.redrawSpinnerTimerTask, 0, 80);
			this.control.update();
		}
	}

	public void stop() {
		if (this.control instanceof Browser
				|| this.control instanceof org.eclipse.swt.browser.Browser) {
			if (this.loader != null) {
				this.loader.stop();
			}
			if (this.shell != null && !this.shell.isDisposed()) {
				this.shell.dispose();
				this.shell = null;
			}
		} else {
			if (this.timer != null) {
				this.timer.cancel();
				this.timer = null;
			}
			if (this.control != null && !this.control.isDisposed()) {
				this.control
						.removePaintListener(this.spinnerDrawingPaintListener);
				this.control.redraw();
				this.control.update();
			}
		}
	}

	public void dispose() {
		this.stop();
	}

	/**
	 * Runs the given callable in a non-UI thread and shows a loader while the
	 * computation is running.
	 * 
	 * @param callable
	 * @return
	 */
	public <T> Future<T> run(final Callable<T> callable) {
		return this.run(callable, true);
	}

	/**
	 * Runs the given callable in a non-UI thread and shows a loader while the
	 * computation is running.
	 * 
	 * @param callable
	 * @return
	 */
	// TODO: move future.isDone() to a queue and check all futures; otherwise
	// only the most recently called run is doind the readAndDispatch which will
	// pause the other runs
	public <T> Future<T> run(final Callable<T> callable,
			final boolean animationEnabled) {
		return ExecUtils.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				if (animationEnabled) {
					Loader.this.start();
				}
				Future<T> future = ExecUtils.nonUIAsyncExec(callable);

				Display display = Display.getCurrent();
				while (!display.isDisposed() && !future.isDone()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}

				T rs = future.get();
				if (animationEnabled) {
					Loader.this.stop();
				}
				return rs;
			}
		});
	}

}
