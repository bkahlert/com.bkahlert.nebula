package com.bkahlert.devel.nebula.widgets.decoration;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.bkahlert.devel.nebula.colors.ColorUtils;
import com.bkahlert.devel.nebula.colors.RGB;
import com.bkahlert.devel.nebula.utils.ExecutorUtil;

/**
 * Class which displays a default text if the user has not entered own input.
 */
public class EmptyText {
	protected Text control;
	protected String emptyText;

	protected Color foregroundColor;
	protected Color emptyTextForegroundColor;

	/**
	 * Construct an {@link EmptyText} field on the specified control, whose
	 * default text is characterized by the specified String.
	 * 
	 * @param control
	 *            the control for which a default text is desired. May not be
	 *            <code>null</code>.
	 * @param defaultText
	 *            the String representing the default text.
	 */
	public EmptyText(Text control, String defaultText) {
		this.control = control;
		this.emptyText = defaultText;

		this.foregroundColor = this.control.getForeground();
		this.emptyTextForegroundColor = new Color(Display.getCurrent(),
				ColorUtils.addLightness(new RGB(this.foregroundColor.getRGB()),
						0.55f).toClassicRGB());

		this.update();
		this.registerListeners();
	}

	protected void registerListeners() {
		this.control.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				EmptyText.this.update(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				EmptyText.this.update(false);
			}
		});

		this.control.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (EmptyText.this.emptyTextForegroundColor != null
						&& !EmptyText.this.emptyTextForegroundColor
								.isDisposed()) {
					EmptyText.this.emptyTextForegroundColor.dispose();
				}
			}
		});
	}

	/**
	 * Updates the {@link Text} {@link Control}'s contents according to its
	 * focus.
	 */
	protected void update() {
		this.update(this.control.isFocusControl());
	}

	/**
	 * Updates the {@link Text} {@link Control}'s contents according to the
	 * provided focus.
	 */
	protected void update(boolean hasFocus) {
		if (hasFocus) {
			if (this.control.getText().equals(this.emptyText)) {
				this.control.setText("");
			} else {
				this.control.selectAll();
			}
			this.control.setForeground(this.foregroundColor);
		} else {
			if (this.control.getText().trim().isEmpty()) {
				/*
				 * Modifying the control's test while the focus out occurs does
				 * not seem to work. We therefore defer the text change.
				 */
				ExecutorUtil.asyncExec(new Runnable() {
					@Override
					public void run() {
						EmptyText.this.control
								.setText(EmptyText.this.emptyText);
						EmptyText.this.control
								.setForeground(EmptyText.this.emptyTextForegroundColor);
					}
				});
			} else {
				this.control.setForeground(this.foregroundColor);
			}
		}
	}

	/**
	 * Returns the underlying {@link Text} {@link Control}.
	 * 
	 * @return
	 */
	public Text getControl() {
		return this.control;
	}

	/**
	 * @see Text#getText()
	 */
	public String getText() {
		if (this.control == null || this.control.isDisposed()) {
			return "";
		}
		return (this.control.getText().equals(this.emptyText)) ? ""
				: this.control.getText();
	}

	/**
	 * @see Text#setText(String)
	 */
	public void setText(final String string) {
		this.control.setText(string);
		this.update();
	}

	/**
	 * @see Text#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.control.setEnabled(enabled);
	}

	/**
	 * @see Text#setFocus()
	 */
	public boolean setFocus() {
		return this.control.setFocus();
	}
}
