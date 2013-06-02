package com.bkahlert.nebula.screenshots.impl.webpage;

import org.apache.log4j.Logger;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.screenshots.webpage.IWebpageFormFiller;

/**
 * Fills out a form while only needing fuzzy information. The filler iterates
 * over an {@link Iterable} and checks for the presence of the given elements.
 * If found the form field's value is set.
 * 
 * @author bkahlert
 * 
 */
public class FuzzyFormFiller implements IWebpageFormFiller {

	public static interface IFieldFill {
		public ISelector getFieldSelector();

		public String getFieldValue();
	}

	public static enum Strategy {
		FILL_FIRST, FILL_ALL;
	}

	private static final Logger LOGGER = Logger
			.getLogger(FuzzyFormFiller.class);

	private Iterable<IFieldFill> fieldFills;
	private Strategy strategy;
	private int wait;

	/**
	 * Constructs a new {@link FuzzyFormFiller} that iterates over the given
	 * {@link IFieldFill}.
	 * 
	 * @param fieldFills
	 * @param strategy
	 *            to use
	 * @param wait
	 *            time this thread sleeps after the last field has been filled.
	 *            Only applies if at least one field was filled.
	 */
	public FuzzyFormFiller(Iterable<IFieldFill> fieldFills, Strategy strategy,
			int wait) {
		this.fieldFills = fieldFills;
		this.strategy = strategy;
		this.wait = wait;
	}

	@Override
	public void fill(final IJQueryEnabledBrowserComposite browserComposite) {
		boolean matched = false;
		for (IFieldFill fieldFill : this.fieldFills) {
			ISelector selector = fieldFill.getFieldSelector();
			try {
				if (browserComposite.containsElements(selector).get()) {
					browserComposite.val(selector, fieldFill.getFieldValue())
							.get();
					matched = true;
					if (this.strategy == Strategy.FILL_FIRST) {
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error filling field " + selector, e);
			}
		}

		if (matched && this.wait > 0) {
			try {
				Thread.sleep(this.wait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}