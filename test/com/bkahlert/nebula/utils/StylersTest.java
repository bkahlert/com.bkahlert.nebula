package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.StyledString;
import org.junit.Test;

public class StylersTest {
	@Test
	public void testShorten() throws Exception {
		assertEquals("abc def 	ghi",
				Stylers.shorten(new StyledString("abc def 	ghi"), 12, 3, "...")
						.getString());
		assertEquals("abc def 	ghi",
				Stylers.shorten(new StyledString("abc def 	ghi"), 5, 3, "...")
						.getString());

		assertEquals("abc def 	ghi",
				Stylers.shorten(new StyledString("abc def 	ghi"), 12, 2, "...")
						.getString());
		assertEquals("abc def...",
				Stylers.shorten(new StyledString("abc def 	ghi"), 5, 2, "...")
						.getString());

		assertEquals("abc def 	ghi",
				Stylers.shorten(new StyledString("abc def 	ghi"), 12, 1, "...")
						.getString());
		assertEquals("abc...",
				Stylers.shorten(new StyledString("abc def 	ghi"), 5, 1, "...")
						.getString());

		assertEquals("abc abc 	abc",
				Stylers.shorten(new StyledString("abc abc 	abc"), 12, 1, "...")
						.getString());
		assertEquals("abc...",
				Stylers.shorten(new StyledString("abc def 	ghi"), 5, 1, "...")
						.getString());
	}
}
