package com.bkahlert.nebula.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CollectionHashMapTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ new SetHashMap<String, String>(HashSet::new) },
				{ new ListHashMap<String, String>(LinkedList::new) } });
	}

	private CollectionHashMap<String, String, Collection<String>> map;

	public CollectionHashMapTest(
			CollectionHashMap<String, String, Collection<String>> map) {
		this.map = map;
	}

	@Test
	public void test() {
		assertEquals(0, this.map.get("a").size());
		assertEquals(0, this.map.get("b").size());

		this.map.addTo("b", "b.1");

		assertEquals(0, this.map.get("a").size());
		assertEquals(1, this.map.get("b").size());

		this.map.addTo("b", "b.2");
		this.map.get("b").add("b.3");

		assertEquals(0, this.map.get("a").size());
		assertEquals(3, this.map.get("b").size());

		this.map.removeFrom("b", "b.3");

		assertEquals(0, this.map.get("a").size());
		assertEquals(2, this.map.get("b").size());

		for (Collection<String> value : this.map.values()) {
			switch (value.size()) {
			case 0:
				assertTrue(true);
				break;
			case 2:
				assertTrue(value.contains("b.1"));
				assertTrue(value.contains("b.2"));
				break;
			default:
				assertTrue(false);
			}
		}

		for (Entry<String, Collection<String>> entry : this.map.entrySet()) {
			if (entry.getKey().equals("a")) {
				assertTrue(true);
			} else if (entry.getKey().equals("b")) {
				assertEquals(2, entry.getValue().size());
				assertTrue(entry.getValue().contains("b.1"));
				assertTrue(entry.getValue().contains("b.2"));
			} else {
				assertTrue(false);
			}
		}
	}

}
