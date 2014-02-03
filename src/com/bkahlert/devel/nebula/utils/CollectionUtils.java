package com.bkahlert.devel.nebula.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {
	public static <SRC, DEST> List<DEST> apply(List<SRC> list,
			IConverter<SRC, DEST> converter) {
		List<DEST> converted = new ArrayList<DEST>(list.size());
		for (SRC src : list) {
			converted.add(converter.convert(src));
		}
		return converted;
	}
}
