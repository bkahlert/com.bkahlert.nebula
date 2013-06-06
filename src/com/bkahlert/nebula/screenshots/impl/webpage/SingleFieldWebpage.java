package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;
import java.util.Iterator;

import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;

public class SingleFieldWebpage extends FormContainingWebpage {

	public SingleFieldWebpage(URI uri, Rectangle bounds, int timeout,
			final ISelector selector, final String value, int wait) {
		super(uri, bounds, timeout, new Iterable<IFieldFill>() {
			@Override
			public Iterator<IFieldFill> iterator() {
				return new Iterator<IFieldFill>() {
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
