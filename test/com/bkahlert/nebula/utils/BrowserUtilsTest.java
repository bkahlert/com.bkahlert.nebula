package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.bkahlert.nebula.widgets.browser.BrowserUtils;
import com.bkahlert.nebula.widgets.browser.extended.html.IElement;

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

		assertEquals(
				"td",
				BrowserUtils
						.getFirstTagName("<td style=\"min-width: 6.5em;\"><div class=\"btn-group\"><a href=\"addcode-apiua://survey/cd/2013-09-19T11:51:16.616+02:00/workStepUnit\" class=\"btn btn-primary btn-xs\">Add Code...</a><a href=\"#\" class=\"btn btn-primary btn-xs\" draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\"apiua://survey/cd/2013-09-19T11:51:16.616+02:00/workStepUnit\"><span class=\"glyphicon glyphicon-share-alt no_click\" style=\"height: 1.5em; line-height: 1.4em;\"></span></a></div></td>"));

		assertEquals(null, BrowserUtils.getFirstTagName("<>"));
		assertEquals(null, BrowserUtils.getFirstTagName("< >"));
		assertEquals(null, BrowserUtils.getFirstTagName("<body"));
		assertEquals(null, BrowserUtils.getFirstTagName("body>"));
		assertEquals(null, BrowserUtils.getFirstTagName(" <bODY"));
		assertEquals(null, BrowserUtils.getFirstTagName("<body<div>>"));
		assertEquals(null, BrowserUtils.getFirstTagName(null));
	}

	@Test
	public void testExtractElement() {
		IElement td = BrowserUtils
				.extractElement("<td style=\"min-width: 6.5em;\"><div class=\"btn-group\"><a href=\"addcode-apiua://survey/cd/2013-09-19T11:51:16.616+02:00/workStepUnit\" class=\"btn btn-primary btn-xs\">Add Code...</a><a href=\"#\" class=\"btn btn-primary btn-xs\" draggable=\"true\" data-dnd-mime=\"text/plain\" data-dnd-data=\"apiua://survey/cd/2013-09-19T11:51:16.616+02:00/workStepUnit\"><span class=\"glyphicon glyphicon-share-alt no_click\" style=\"height: 1.5em; line-height: 1.4em;\"></span></a></div></td>");
		assertNotNull(td);
		assertEquals("td", td.getName());

		IElement selfEnclosingTd = BrowserUtils
				.extractElement("<td style=\"min-width: 6.5em;\"/>");
		assertNotNull(selfEnclosingTd);
		assertEquals("td", selfEnclosingTd.getName());
	}

}
