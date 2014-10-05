package com.bkahlert.nebula.gallery.demoSuits;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;
import com.bkahlert.nebula.utils.ExecUtils;
import com.bkahlert.nebula.widgets.decoration.EmptyText;

public abstract class AbstractDemo {

	private static final Logger LOGGER = Logger.getLogger(AbstractDemo.class);
	private static AbstractDemo currentDemo = null;

	/**
	 * Contains all controls and the demoAreaContent.
	 */
	protected Composite composite;
	protected Composite controls;
	protected Composite content;

	/**
	 * Contains the console for debug information send by the
	 * {@link AbstractDemo} instance.
	 */
	protected EmptyText console;
	protected static final SimpleDateFormat consoleDateFormat = new SimpleDateFormat(
			"HH:mm:ss");

	protected int consoleHeight = 150;

	public final void createPartControls(Composite composite) {
		this.composite = composite;
		this.composite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0)
				.create());

		this.controls = new Composite(this.composite, SWT.NONE);
		this.controls.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());

		this.content = new Composite(this.composite, SWT.NONE);
		this.content.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, true).create());

		Text console = new Text(this.composite, SWT.V_SCROLL | SWT.BORDER
				| SWT.MULTI);
		console.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		this.console = new EmptyText(console, "Debug Console");
		this.hideConsole();

		currentDemo = this;

		this.recreateDemo();
	}

	/**
	 * Shows the console
	 */
	public void showConsole() {
		((GridData) this.console.getControl().getLayoutData()).heightHint = this.consoleHeight;
		this.console.getControl().setVisible(true);
		this.composite.layout();
	}

	/**
	 * Hides the console
	 */
	public void hideConsole() {
		((GridData) this.console.getControl().getLayoutData()).heightHint = 0;
		this.console.getControl().setVisible(false);
		this.composite.layout();
	}

	/**
	 * Adds a message to the console and shows it if hidden.
	 * 
	 * @param message
	 */
	public static void log(final String message) {
		if (currentDemo == null) {
			return;
		}

		try {
			ExecUtils.syncExec(new Runnable() {
				@Override
				public void run() {
					String newLine = consoleDateFormat.format(new Date()) + " "
							+ message + "\n";
					String oldText = currentDemo.console.getText();
					String newText = oldText + newLine;
					currentDemo.console.setText(newText);
					Text control = currentDemo.console.getControl();
					if (control != null && !control.isDisposed()) {
						control.setSelection(newText.length());
					}
					currentDemo.showConsole();
				}
			});
		} catch (SWTException e) {
			// Disposed
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}

	public static void log(Throwable e) {
		log(e.getMessage());
	}

	/**
	 * Creates the controls for this demo.
	 * <p>
	 * By default they are hidden.
	 * 
	 * @param composite
	 */
	public void createControls(Composite composite) {
		this.controls.setVisible(false);
		((GridData) this.controls.getLayoutData()).heightHint = 0;
	}

	/**
	 * Creates the demoAreaContent for this demo.
	 * 
	 * @param composite
	 */
	public void createDemo(Composite composite) {
		return;
	}

	public void dispose() {
		currentDemo = null;
	}

	/**
	 * Recreates the demo. Especially useful if debug mode is enabled.
	 */
	public void recreateDemo() {
		CompositeUtils.emptyComposite(this.controls);
		CompositeUtils.emptyComposite(this.content);
		RowLayout rowLayout = new RowLayout();
		rowLayout.fill = true;
		this.controls.setLayout(rowLayout);
		this.content.setLayout(new FillLayout());
		this.createControls(this.controls);
		this.createDemo(this.content);
		this.composite.layout();
	}

	public void layout() {
		this.composite.layout();
	}
}
