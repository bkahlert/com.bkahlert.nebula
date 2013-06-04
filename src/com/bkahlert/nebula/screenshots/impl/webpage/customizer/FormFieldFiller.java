package com.bkahlert.nebula.screenshots.impl.webpage.customizer;

import java.util.Iterator;

import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;

/**
 * Fills out a form field.
 * 
 * @author bkahlert
 * 
 */
public class FormFieldFiller extends FuzzyFormFiller {

	public FormFieldFiller(final ISelector selector, final String value,
			int wait) {
		super(new Iterable<IFieldFill>() {
			@Override
			public Iterator<IFieldFill> iterator() {
				return new Iterator<FuzzyFormFiller.IFieldFill>() {
					private boolean returned = false;

					@Override
					public void remove() {
					}

					@Override
					public IFieldFill next() {
						return !this.returned ? new IFieldFill() {

							@Override
							public ISelector getFieldSelector() {
								return selector;
							}

							@Override
							public String getFieldValue() {
								return value;
							}
						} : null;
					}

					@Override
					public boolean hasNext() {
						return !this.returned;
					}
				};
			}
		}, Strategy.FILL_FIRST, wait);
	}

}