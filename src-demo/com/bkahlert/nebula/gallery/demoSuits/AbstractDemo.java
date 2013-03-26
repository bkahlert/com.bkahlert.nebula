package com.bkahlert.nebula.gallery.demoSuits;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.widgets.decoration.EmptyText;
import com.bkahlert.nebula.gallery.util.deprecated.CompositeUtils;

public abstract class AbstractDemo {

	/**
	 * Contains all controls and the content.
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

	public void createPartControls(Composite composite) {
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
	public void addConsoleMessage(String message) {
		final String newLine = consoleDateFormat.format(new Date()) + " "
				+ message + "\n";
		String oldText = this.console.getText();
		String newText = oldText + newLine;
		this.console.setText(newText);
		this.console.getControl().setSelection(newText.length());
		this.showConsole();
	}

	/**
	 * Creates the controls for this demo.
	 * <p>
	 * By default they are hidden.
	 * 
	 * @param composite
	 */
	public void createControls(Composite composite) {
		composite.setVisible(false);
	}

	/**
	 * Creates the content for this demo.
	 * 
	 * @param composite
	 */
	public void createDemo(Composite composite) {
		return;
	}

	public void dispose() {

	}

	/**
	 * Recreates the demo. Especially useful if debug mode is enabled.
	 */
	public void recreateDemo() {
		CompositeUtils.emptyComposite(this.controls);
		CompositeUtils.emptyComposite(this.content);
		this.controls.setLayout(new RowLayout());
		this.createControls(this.controls);
		this.content.setLayout(new FillLayout());
		this.createDemo(this.content);
		this.composite.layout();
	}

	public void layout() {
		this.composite.layout();
	}
}
