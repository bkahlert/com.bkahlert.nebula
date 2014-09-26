package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.bkahlert.nebula.widgets.browser.BrowserUtils;

public class BrowserUtilsTest {

	@Test
	public void testGetFirstTagName() {
		assertEquals("body", BrowserUtils.getFirstTagName("<body>"));
		assertEquals("body", BrowserUtils.getFirstTagName(" <body>"));
		assertEquals("body", BrowserUtils.getFirstTagName("<body>"));
		assertEquals("body", BrowserUtils.getFirstTagName("  <body>  \n"));
		assertEquals("bODY",
				BrowserUtils.getFirstTagName(" abc <bODY a=\"b\"> def"));

		assertEquals("body", BrowserUtils.getFirstTagName("<body/>"));
		assertEquals("body", BrowserUtils.getFirstTagName(" <body/>"));
		assertEquals("body", BrowserUtils.getFirstTagName("<body/>"));
		assertEquals("body", BrowserUtils.getFirstTagName("  <body />  \n"));
		assertEquals("bODY",
				BrowserUtils.getFirstTagName(" abc <bODY a=\'b\' /> def"));

		assertEquals("body",
				BrowserUtils.getFirstTagName("<body><div></div></body>"));
		assertEquals("body",
				BrowserUtils.getFirstTagName(" <body><div></div></body>"));
		assertEquals("body",
				BrowserUtils.getFirstTagName("<body><div></div></body>"));
		assertEquals(
				"body",
				BrowserUtils
						.getFirstTagName("  <body a=\"b\"><div></div></body>  \n"));
		assertEquals(
				"bODY",
				BrowserUtils
						.getFirstTagName(" abc <bODY a=\'b\' ><div></div></bODY> def"));

		assertEquals(
				"a",
				BrowserUtils
						.getFirstTagName("<a class=\"link-box\" href=\"//ru.wikipedia.org/\" title=\"Russkiy — Wikipedia — Свободная энциклопедия\"><strong>Русский</strong><br>\n		<em>Свободная энциклопедия</em><br>\n		<small>1 144 000+ статей</small></a>"));

		assertEquals(null, BrowserUtils.getFirstTagName("<>"));
		assertEquals(null, BrowserUtils.getFirstTagName("< >"));
		assertEquals(null, BrowserUtils.getFirstTagName("<body"));
		assertEquals(null, BrowserUtils.getFirstTagName("body>"));
		assertEquals(null, BrowserUtils.getFirstTagName(" <bODY"));
		assertEquals(null, BrowserUtils.getFirstTagName("<body<div>>"));
		assertEquals(null, BrowserUtils.getFirstTagName(null));
	}

}
