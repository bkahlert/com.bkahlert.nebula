package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.bkahlert.nebula.utils.CollectionUtils;
import com.bkahlert.nebula.utils.IConverter;

public class CollectionUtilsTest {

	private static final List<String> STRING_LIST = new ArrayList<String>(
			Arrays.asList("Hello World!", "123", "3.141f"));

	@Test
	public void testApply() {
		assertEquals(Arrays.asList(null, 123f, 3.141f), CollectionUtils.apply(
				STRING_LIST, new IConverter<String, Float>() {
					@Override
					public Float convert(String returnValue) {
						try {
							return Float.valueOf(returnValue);
						} catch (Exception e) {
							return null;
						}
					}
				}));
	}

}
