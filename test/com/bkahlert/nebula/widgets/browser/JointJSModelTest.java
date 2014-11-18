package com.bkahlert.nebula.widgets.browser;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;

import com.bkahlert.nebula.widgets.jointjs.JointJSLink;
import com.bkahlert.nebula.widgets.jointjs.JointJSModel;

public class JointJSModelTest {

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidJSON() throws Exception {
		new JointJSModel("abc");
	}

	@SuppressWarnings("serial")
	@Test
	public void test() throws Exception {
		String json = "{\"title\": \"Test\", \"cells\":[{\"type\":\"html.Element\",\"position\":{\"x\":0,\"y\":35},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"apiua://code1\",\"title\":\"my box\",\"content\":\"<ul><li>jkjk</li></ul>\",\"z\":0,\"attrs\":{}},{\"type\":\"html.Element\",\"position\":{\"x\":107.55000000000001,\"y\":150},\"size\":{\"width\":130,\"height\":30},\"angle\":0,\"id\":\"apiua://code2\",\"title\":\"my box233333\",\"z\":1,\"content\":\"XN dskjd sdkds dskdsdjks dskj \",\"attrs\":{}},{\"type\":\"link\",\"className\":\"test\",\"source\":{\"id\":\"apiua://code1\"},\"target\":{\"id\":\"apiua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"my_label\"}}}],\"id\":\"apiua://link1\",\"smooth\":true,\"permanent\":true,\"z\":2,\"attrs\":{\".marker-source\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"},\".connection\":{\"stroke-dasharray\":\"1,4\"}}},{\"type\":\"html.Element\",\"position\":{\"x\":450,\"y\":200},\"size\":{\"width\":300,\"height\":100},\"angle\":0,\"id\":\"apiua://code3\",\"title\":\"my box233333\",\"z\":3,\"attrs\":{}},{\"type\":\"link\",\"source\":{\"id\":\"apiua://code3\"},\"target\":{\"id\":\"apiua://code2\"},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"Label2\"}}}],\"id\":\"apiua://link2\",\"smooth\":true,\"z\":4,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}},{\"type\":\"link\",\"source\":{\"id\":\"apiua://code3\"},\"target\":{\"x\":1000,\"y\":250},\"labels\":[{\"position\":0.5,\"attrs\":{\"text\":{\"text\":\"I'm pointing to nowhere\"}}}],\"id\":\"apiua://link3\",\"smooth\":true,\"z\":5,\"attrs\":{\".marker-target\":{\"d\":\"M 10 0 L 0 5 L 10 10 z\"}}}]}";

		JointJSModel model = new JointJSModel(json);

		assertEquals("Test", model.getTitle());
		model.setTitle("New Title");
		assertEquals("New Title", model.getTitle());

		String element1 = "apiua://code1";
		String element2 = "apiua://code2";
		String element3 = "apiua://code3";
		assertEquals(
				Arrays.asList(element1, element2, element3),
				model.getElements().stream().map(e -> e.getId())
						.collect(Collectors.toList()));

		// contains only 1 non-permanent link
		assertEquals(2, model.getLinks().size());

		JointJSLink link1 = model.getLinks().get(0);
		assertEquals("Label2", link1.getTitle());
		assertEquals(new JointJSLink.ElementEndpoint(element3.toString()),
				link1.getSource());
		assertEquals(new JointJSLink.ElementEndpoint(element2.toString()),
				link1.getTarget());

		JointJSLink link2 = model.getLinks().get(1);
		assertEquals("I'm pointing to nowhere", link2.getTitle());
		assertEquals(new JointJSLink.ElementEndpoint(element3.toString()),
				link2.getSource());
		assertEquals(new JointJSLink.CoordinateEndpoint(1000, 250),
				link2.getTarget());
	}
}
