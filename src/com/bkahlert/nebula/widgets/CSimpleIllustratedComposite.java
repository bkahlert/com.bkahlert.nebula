package com.bkahlert.nebula.widgets;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * This composite displays a simple {@link IllustratedComposite} that can only
 * display text.
 * <p>
 * In contrast to the classical {@link SimpleIllustratedComposite} this one
 * internally uses a {@link CLabel} instead of a {@link Label}.
 * <p>
 * This composite does <strong>NOT</strong> handle setting the layout and adding
 * sub {@link Control}s correctly.
 * <p>
 * <img src="doc-files/SimpleIllustratedComposite-1.png"/>
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BOLD and those supported by {@link IllustratedComposite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @see IllustratedComposite
 * @author bkahlert
 * 
 */
public class CSimpleIllustratedComposite extends SimpleIllustratedComposite {

	public CSimpleIllustratedComposite(Composite parent, int style) {
		super(parent, style);
	}

	public CSimpleIllustratedComposite(Composite parent, int style,
			IllustratedText illustratedText) {
		super(parent, style, illustratedText);
	}

	protected void createContentLabel(int style) {
		this.contentLabel = new CLabel(this, style);
	}

	protected void setContentLabelText(String text) {
		((CLabel) this.contentLabel).setText(text);
	}

	protected Control getContentLabel() {
		return this.contentLabel;
	}
}
