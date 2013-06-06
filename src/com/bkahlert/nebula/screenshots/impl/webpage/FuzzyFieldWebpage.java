package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Fills out a form while only needing fuzzy information. The filler iterates
 * over an {@link Iterable} and checks for the presence of the given elements.
 * If found the form field's value is set.
 * 
 * @author bkahlert
 * 
 */
public class FuzzyFieldWebpage extends FormContainingWebpage {

	public FuzzyFieldWebpage(URI uri, Rectangle bounds, int timeout,
			Iterable<IFieldFill> fieldFills, int wait) {
		super(uri, bounds, timeout, fieldFills, Strategy.FILL_FIRST, wait);
	}

}
