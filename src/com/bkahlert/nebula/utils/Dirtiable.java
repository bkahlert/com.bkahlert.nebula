package com.bkahlert.nebula.utils;

public class Dirtiable implements IDirtiable {

	private long lastModified;

	public Dirtiable() {
		this.modified();
	}

	@Override
	public void modified() {
		this.lastModified = System.nanoTime();
	}

	@Override
	public long getLastModification() {
		return this.lastModified;
	}

}
