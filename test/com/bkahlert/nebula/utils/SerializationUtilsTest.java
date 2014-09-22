package com.bkahlert.nebula.utils;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Point;
import org.junit.Test;

public class SerializationUtilsTest {
	@Test
	public void simpleByte() throws Exception {
		String string = "Hello World!";

		String ser = SerializationUtils.serialize(string);

		String unser = SerializationUtils.deserialize(ser, String.class);
		assertEquals(string, unser);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void complexMap() throws Exception {
		HashMap<String, Point> scrollPositions = new HashMap<String, Point>();
		scrollPositions.put("1", new Point(2, 3));
		scrollPositions.put("4", new Point(5, 6));
		Map<String, String> sMap = new HashMap<String, String>();
		for (Entry<String, Point> entry : scrollPositions.entrySet()) {
			if (entry.getValue() != null) {
				sMap.put(entry.getKey(),
						entry.getValue().x + "," + entry.getValue().y);
			}
		}

		String ser = SerializationUtils.serialize((Serializable) sMap);

		Map<String, String> dMap = new HashMap<String, String>();
		dMap = (Map<String, String>) SerializationUtils.deserialize(ser,
				Serializable.class);
		for (Entry<String, String> entry : dMap.entrySet()) {
			if (entry.getValue().contains(",")) {
				String[] split = entry.getValue().split(",");
				try {
					scrollPositions.put(
							entry.getKey(),
							new Point(Integer.parseInt(split[0]), Integer
									.parseInt(split[1])));
				} catch (NumberFormatException e) {
				}
				;
			}
		}
	}
}
