package com.bkahlert.devel.nebula.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DependencyGraphTest {

	@Test
	public void test() throws CircleException {
		DependencyGraph<Integer> graph = new DependencyGraph<Integer>();

		assertFalse(graph.isInGraph(1));
		graph.addNode(1, null);
		assertEquals(0, graph.getDependants(1).size());
		assertTrue(graph.isInGraph(1));

		assertFalse(graph.isInGraph(2));
		graph.addNode(2, new ArrayList<Integer>());
		assertEquals(0, graph.getDependants(2).size());
		assertTrue(graph.isInGraph(2));

		// one dependency
		assertFalse(graph.isInGraph(3));
		graph.addNode(3, Arrays.asList(2));
		assertEquals(0, graph.getDependants(3).size());
		assertTrue(graph.isInGraph(3));

		assertEquals(1, graph.getDependants(2).size());
		assertEquals(3, (int) graph.getDependants(2).get(0));

		// two dependency
		assertFalse(graph.isInGraph(4));
		graph.addNode(4, Arrays.asList(1, 3));
		assertEquals(0, graph.getDependants(4).size());
		assertTrue(graph.isInGraph(4));

		assertEquals(1, graph.getDependants(1).size());
		assertEquals(4, (int) graph.getDependants(1).get(0));

		assertEquals(1, graph.getDependants(3).size());
		assertEquals(4, (int) graph.getDependants(3).get(0));

		// one dependency to unknown element
		assertFalse(graph.isInGraph(5));
		graph.addNode(5, Arrays.asList(1, 6));
		assertEquals(0, graph.getDependants(5).size());
		assertTrue(graph.isInGraph(5));

		assertEquals(2, graph.getDependants(1).size());
		assertEquals(4, (int) graph.getDependants(1).get(0));
		assertEquals(5, (int) graph.getDependants(1).get(1));

		assertEquals(1, graph.getDependants(6).size());
		assertEquals(5, (int) graph.getDependants(6).get(0));
	}

	@Test(expected = CircleException.class)
	public void testCircles() throws Exception {
		DependencyGraph<Integer> graph = new DependencyGraph<Integer>();
		graph.addNode(1, Arrays.asList(2));
		graph.addNode(2, Arrays.asList(1));
	}

	@Test
	public void testIterator() throws CircleException {
		DependencyGraph<Integer> graph = new DependencyGraph<Integer>();
		graph.addNode(1, Arrays.asList(2, 3));
		graph.addNode(2, Arrays.asList(3));
		graph.addNode(4, Arrays.asList(3));
		graph.addNode(5, null);

		int[][] nodes = new int[][] { { 3, 5 }, { 2, 4 }, { 1 } };
		int i = 0;
		for (List<Integer> elements : graph) {
			assertEquals(nodes[i].length, elements.size());
			for (int element : nodes[i]) {
				assertTrue(elements.contains(element));
			}
			i++;
		}
		assertEquals(nodes.length, i);
	}

}
