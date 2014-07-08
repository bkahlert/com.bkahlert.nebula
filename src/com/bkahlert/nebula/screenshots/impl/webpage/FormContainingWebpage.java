package com.bkahlert.nebula.screenshots.impl.webpage;

import java.net.URI;

import org.eclipse.swt.graphics.Rectangle;

import com.bkahlert.nebula.screenshots.webpage.IFormContainingWebpage;

public class FormContainingWebpage extends Webpage implements
		IFormContainingWebpage {

	private Iterable<IFieldFill> fieldFills;
	private Strategy strategy;
	private int wait;

	public FormContainingWebpage(URI uri, Rectangle bounds, int timeout,
			Iterable<IFieldFill> fieldFills, Strategy strategy, int wait) {
		super(uri, bounds, timeout);
		this.fieldFills = fieldFills;
		this.strategy = strategy;
		this.wait = wait;
	}

	@Override
	public Iterable<IFieldFill> getFieldFills() {
		return this.fieldFills;
	}

	@Override
	public Strategy getStrategy() {
		return this.strategy;
	}

	@Override
	public int getWait() {
		return this.wait;
	}

}
