package com.bkahlert.nebula.screenshots.impl.webpage;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import com.bkahlert.devel.nebula.widgets.browser.extended.IJQueryEnabledBrowserComposite;
import com.bkahlert.devel.nebula.widgets.browser.extended.ISelector;
import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage;
import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage.IFieldFill;
import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage.Strategy;

public class FormContainingWebpageScreenshotRenderer<WEBPAGE extends IFormContainingWebpage>
		extends WebpageScreenshotRenderer<WEBPAGE> {

	private static final Logger LOGGER = Logger
			.getLogger(FormContainingWebpageScreenshotRenderer.class);

	public FormContainingWebpageScreenshotRenderer(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void loadingWebpageFinished(WEBPAGE webpage,
			IJQueryEnabledBrowserComposite browser) {
		Iterable<IFieldFill> fieldFills = webpage.getFieldFills();
		if (fieldFills == null) {
			return;
		}

		boolean matched = false;
		for (IFieldFill fieldFill : fieldFills) {
			ISelector selector = fieldFill.getFieldSelector();
			try {
				if (browser.containsElements(selector).get()) {
					browser.simulateTyping(selector, fieldFill.getFieldValue())
							.get();
					matched = true;
					if (webpage.getStrategy() == Strategy.FILL_FIRST) {
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("Error filling field " + selector, e);
			}
		}

		if (matched && webpage.getWait() > 0) {
			try {
				Thread.sleep(webpage.getWait());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

}
