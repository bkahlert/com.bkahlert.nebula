package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class JSONUtilsTest {
	@Test
	public void testIntegersNotConvertedToStrings() throws Exception {
		String jsonStr = "{\"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":161,\"y\":140},\"size\":{\"width\":97,\"height\":30},\"angle\":0,\"id\":\"apiua://code/-9223372036854775585\",\"content\":\"\",\"title\":\"Ungeklärt\",\"z\":0,\"color\":\"rgb(0, 0, 0)\",\"background-color\":\"rgba(106, 198, 57, 0.27450980392156865)\",\"border-color\":\"rgba(90, 168, 48, 0.39215686274509803)\",\"attrs\":{}}],\"title\":\"New Model\",\"zoom\":1,\"pan\":{\"x\":0,\"y\":0}}";

		Object jsonObj = JSONUtils.parseJson(jsonStr);
		@SuppressWarnings("unchecked")
		HashMap<Object, Object> position = (HashMap<Object, Object>) ((HashMap<Object, Object>) ((List<Object>) ((HashMap<Object, Object>) jsonObj)
				.get("cells")).get(0)).get("position");
		assertEquals(Integer.class, position.get("x").getClass());
		assertEquals(Integer.class, position.get("y").getClass());

		String jsonStr2 = JSONUtils.buildJson(jsonObj);
		assertEquals(jsonStr, jsonStr2);
	}

	@Test
	public void testEncoding() throws Exception {
		String text = "ÄÖÜäöüß";
		String jsonStr = "{\"text\":\"" + text + "\"}";

		Object jsonObj = JSONUtils.parseJson(jsonStr);
		@SuppressWarnings("unchecked")
		String jsonText = (String) ((HashMap<Object, Object>) jsonObj)
				.get("text");
		assertEquals(text, jsonText);

		String jsonStr2 = JSONUtils.buildJson(jsonObj);
		assertEquals(jsonStr, jsonStr2);
	}
}
