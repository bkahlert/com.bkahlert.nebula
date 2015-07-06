package com.bkahlert.nebula.utils.replacement;

import org.eclipse.core.runtime.Assert;

public class Replacement implements IReplacement {
	
	private String name;
	private String value;
	
	public Replacement(String name, String value) {
		super();
		Assert.isNotNull(name);
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

}
