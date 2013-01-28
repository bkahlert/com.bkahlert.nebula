package com.bkahlert.devel.nebula.data;

public class CircleException extends RuntimeException {

	private static final long serialVersionUID = -1516838711574368451L;

	public CircleException(DependencyGraph<?> graph, Object dependee,
			Object dependant) {
		super("Adding " + dependant + " as an depenant node to " + dependee
				+ " in graph would reside in a circle.");
	}

}
