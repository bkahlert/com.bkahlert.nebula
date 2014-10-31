package com.bkahlert.nebula.utils;

public interface IReflexiveConverter<SRC, DEST> extends IConverter<SRC, DEST> {
	public SRC convertBack(DEST object);
}
