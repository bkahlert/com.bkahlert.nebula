package com.bkahlert.devel.nebula.widgets.browser.extended;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.bkahlert.devel.nebula.utils.ExecutorUtil;
import com.bkahlert.devel.nebula.widgets.browser.IBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.IBrowserCompositeExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryExtension;
import com.bkahlert.devel.nebula.widgets.browser.extended.extensions.JQueryScrollToExtension;

public class JQueryEnabledBrowserComposite extends ExtendedBrowserComposite
		implements IJQueryEnabledBrowserComposite {
	private static final Logger LOGGER = Logger
			.getLogger(JQueryEnabledBrowserComposite.class);

	public JQueryEnabledBrowserComposite(Composite parent, int style) {
		super(parent, style, new IBrowserCompositeExtension[] {
				new JQueryExtension(), new JQueryScrollToExtension() });
	}

	@Override
	public Future<Boolean> containsElements(ISelector selector) {
		return this.run("return $('" + selector.toString() + "').length > 0;",
				IBrowserComposite.CONVERTER_BOOLEAN);
	}

	@Override
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

	@Override
	public Future<Boolean> scrollTo(Point pos) {
		return this.scrollTo(pos.x, pos.y);
	}

	@Override
	public Future<Object> val(ISelector selector, String value) {
		return this.run("$('" + selector + "').val('" + value + "');");
	}

	@Override
	public Future<Object> submit(ISelector selector) {
		return this.run("$('" + selector + "').closest('form').submit();");
	}

}
