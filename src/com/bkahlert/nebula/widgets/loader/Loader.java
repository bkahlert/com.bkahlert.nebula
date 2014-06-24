package com.bkahlert.nebula.widgets.loader;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.utils.ImageUtils;
import com.bkahlert.nebula.utils.MathUtils;
import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class Loader {

	private static final Logger LOGGER = Logger.getLogger(Loader.class);

	private static final File SPINNER_FILE = BrowserUtils.getFile(Loader.class,
			"spinner.png");
	private static final Image SPINNER = new Image(Display.getCurrent(),
			SPINNER_FILE.getAbsolutePath());
	private static final int NUM_SPINNER_SPRITES = 12;

	private static Dimension SPINNER_DIMENSIONS;
	static {
		try {
			SPINNER_DIMENSIONS = ImageUtils.getImageDimensions(SPINNER_FILE);
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

	private final Shell shell = null;
	private final Control control;

	private final ControlListener controlListener = new ControlListener() {
		@Override
		public void controlResized(ControlEvent e) {
			Loader.this.updateBounds();
		}

		@Override
		public void controlMoved(ControlEvent e) {
			Loader.this.updateBounds();
		}
	};
	private final PaintListener paintListener = new PaintListener() {
		private int sprite = 0;

		@Override
		public void paintControl(PaintEvent e) {
			Point controlSize = Loader.this.control.getSize();
			int size = MathUtils.min(SPINNER_DIMENSIONS.height, controlSize.x,
					controlSize.y) / 2;

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

	public Loader(Control control) {
		super();
		Assert.isNotNull(control);
		this.control = control;
	}

	public void start() {
		// if (this.shell == null) {
		// this.shell = new Shell(SWT.NO_TRIM | SWT.ON_TOP);
		// this.shell.setLayout(new FillLayout());
		// new LoaderComposite(this.shell);
		// }
		this.activate();
		// this.updateBounds();
		// this.shell.setAlpha(125);
		// this.shell.open();
	}

	protected void updateBounds() {
		this.shell.setSize(this.control.getSize());
		this.shell.setLocation(this.control.toDisplay(0, 0));
	}

	protected void activate() {
		// Control control = this.control;
		// while (control != null) {
		// control.addControlListener(this.controlListener);
		// control = control.getParent();
		// }

		this.control.addPaintListener(this.paintListener);
		this.control.redraw();
	}

	protected void deactivate() {
		this.control.removePaintListener(this.paintListener);
		this.control.redraw();

		// Control control = this.control;
		// while (control != null) {
		// control.removeControlListener(this.controlListener);
		// control = control.getParent();
		// }
	}

	public void stop() {
		this.deactivate();
		// if (this.shell != null && !this.shell.isDisposed()) {
		// this.shell.setVisible(false);
		// }
	}

	public void dispose() {
		this.deactivate();
		// if (this.shell != null && !this.shell.isDisposed()) {
		// this.shell.close();
		// this.shell.dispose();
		// }
	}

	/**
	 * Runs the given callable in a non-UI thread and shows a loader while the
	 * computation is running.
	 * 
	 * @param callable
	 * @return
	 */
	public <T> Future<T> run(final Callable<T> callable) {
		return ExecUtils.asyncExec(new Callable<T>() {
			@Override
			public T call() throws Exception {
				Loader.this.start();
				Future<T> future = ExecUtils.nonUIAsyncExec(callable);

				Display display = Display.getCurrent();
				while (!display.isDisposed() && !future.isDone()) {
					Loader.this.control.redraw();
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}

				T rs = future.get();
				Loader.this.stop();
				return rs;
			}
		});
	}

}
