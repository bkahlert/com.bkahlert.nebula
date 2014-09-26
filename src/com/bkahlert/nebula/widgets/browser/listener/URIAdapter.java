package com.bkahlert.nebula.widgets.browser.listener;

import java.net.URI;

public class URIAdapter implements IURIListener {

	@Override
	@Deprecated
	public void uriClicked(URI uri) {
		return;
	}

	@Override
	public void uriHovered(URI uri, boolean entered) {
		return;
	}

}
