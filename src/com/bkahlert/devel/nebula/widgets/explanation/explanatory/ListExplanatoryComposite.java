package com.bkahlert.devel.nebula.widgets.explanation.explanatory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite;
import com.bkahlert.devel.nebula.widgets.explanation.ListExplanationComposite.ListExplanation;


/**
 * Instances of this class are controls which are capable of containing exactly
 * <strong>one</strong> demoAreaContent {@link Control} and an implicitly generated
 * {@link ListExplanationComposite} to display explanatory information.<br>
 * Although this composite may be subclasses <strong>only the control registered
 * trough {@link ListExplanatoryComposite#setContentControl(Control)} can be
 * displayed</strong>.
 * 
 * <p>
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>{@link SWT#H_SCROLL}, {@link SWT#V_SCROLL}, {@link SWT#BORDER} - these
 * styles are only applied on the {@link ListExplanationComposite} not on the
 * {@link ListExplanatoryComposite} itself</dd>
 * <dd>Styles supported by {@link Composite}</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @see ListExplanationComposite
 * @see Composite
 * @author bkahlert
 * 
 */
public class ListExplanatoryComposite extends ExplanatoryComposite {
	protected ListExplanationComposite explanationComposite;

	/**
	 * Constructs a new {@link ListExplanatoryComposite} with a given parent and
	 * the passed style information.
	 * 
	 * @param parent
	 * @param style
	 */
	public ListExplanatoryComposite(Composite parent, int style) {
		super(parent, style & ~(SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));

		this.explanationComposite = new ListExplanationComposite(this, style
				& (SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
	}

	/**
	 * Hides the demoAreaContent and displays an explanation.
	 * 
	 * @param listExplanation
	 *            The explanation to be displayed; if null the explanation gets
	 *            hidden
	 */
	public void showExplanation(ListExplanation listExplanation) {
		if (listExplanation == null) {
			super.hideExplanation();
		} else {
			this.explanationComposite.setExplanation(listExplanation);
			super.showExplanation(this.explanationComposite);
		}
	}

	/**
	 * Hides the explanation and displays the demoAreaContent.
	 */
	@Override
	public void hideExplanation() {
		super.showExplanation(null);
	}
}
