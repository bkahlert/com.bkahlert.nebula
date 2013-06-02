package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryScrollToExtension;

public class JQueryEnabledBrowserComposite extends ExtendedBrowserComposite {
	private static final Logger LOGGER = Logger
			.getLogger(JQueryEnabledBrowserComposite.class);

	public JQueryEnabledBrowserComposite(Composite parent, int style) {
		super(parent, style, new IBrowserCompositeExtension[] {
				new JQueryExtension(), new JQueryScrollToExtension() });
	}

	/**
	 * Scrolls to the given position.
	 * 
	 * @param x
	 * @param y
	 * @return false if no scroll action was necessary
	 */
	public Future<Boolean> scrollTo(final int x, final int y) {
		return ExecutorUtil.nonUIAsyncExec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					String script = String
							.format("if(jQuery(document).scrollLeft()!=%d||jQuery(document).scrollTop()!=%d){jQuery.scrollTo({left:'%dpx',top:'%dpx'}, 0);return true;}else{return false;}",
									x, y, x, y);
					return JQueryEnabledBrowserComposite.this.run(script,
							CONVERTER_BOOLEAN).get();
				} catch (Exception e) {
					LOGGER.error("Error scrolling", e);
				}
				return null;
			}
		});
	}

	/**
	 * Sets the given value of the elements specified by the given selector.
	 * 
	 * @param selector
	 * @param value
	 * 
	 * @see <a href="http://api.jquery.com/val/">api.jquery.com/val/</a>
	 */
	public Future<Object> val(String selector, String value) {
		return this.run("$('" + selector + "').val('" + value + "');");
	}

}
