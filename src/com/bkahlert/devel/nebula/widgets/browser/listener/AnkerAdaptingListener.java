package com.bkahlert.devel.nebula.widgets.browser.listener;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

import com.bkahlert.devel.nebula.widgets.browser.IAnker;

/**
 * Instances of this class adapt {@link IURIListener}s so they can be used as
 * {@link IAnkerListener}s.
 * 
 * @author bkahlert
 * 
 */
public class AnkerAdaptingListener implements IAnkerListener {

	private static final Logger LOGGER = Logger
			.getLogger(AnkerAdaptingListener.class);

	private IURIListener uriListener;

	public AnkerAdaptingListener(IURIListener uriListener) {
		Assert.isNotNull(uriListener);
		this.uriListener = uriListener;
	}

	@Override
	public void ankerClicked(IAnker anker) {
		try {
			this.uriListener.uriClicked(new URI(anker.getHref()));
		} catch (URISyntaxException e) {
			LOGGER.info("Error converting " + anker.getHref() + " to a "
					+ URI.class.getSimpleName(), e);
		}
	}

	@Override
	public void ankerHovered(IAnker anker, boolean entered) {
		try {
			this.uriListener.uriHovered(new URI(anker.getHref()), entered);
		} catch (URISyntaxException e) {
			LOGGER.info("Error converting " + anker.getHref() + " to a "
					+ URI.class.getSimpleName(), e);
		}
	}

}
