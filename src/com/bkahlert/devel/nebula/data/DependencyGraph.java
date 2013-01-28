package com.bkahlert.devel.nebula.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;

public class DependencyGraph<T> implements Iterable<List<T>> {

	private Map<T, List<T>> values = new HashMap<T, List<T>>();

	public void addNode(T value, Collection<T> dependentOns)
			throws CircleException {
		if (!values.containsKey(value))
			values.put(value, new ArrayList<T>());

		if (dependentOns != null && dependentOns.size() > 0) {
			for (T dependentOn : dependentOns) {
				if (!values.containsKey(dependentOn)) {
					values.put(dependentOn, new ArrayList<T>());
				} else {
					if (values.get(dependentOn).contains(value))
						throw new CircleException(this, value, dependentOn);
				}
				values.get(value).add(dependentOn);
			}
		}
	}

	public boolean isInGraph(T data) {
		return values.containsKey(data);
	}

	public List<T> getDependants(T value) {
		List<T> dependants = new ArrayList<T>();
		for (T element : values.keySet()) {
			if (values.get(element).contains(value))
				dependants.add(element);
		}
		return dependants;
	}

	/**
	 * Returns a {@link List} of values in the way that all dependencies can be
	 * satisfied.
	 * <p>
	 * A dependency is satisfied is all elements the current one relies on are
	 * already part of the {@link List}.
	 * 
	 * @return
	 */
	public List<List<T>> getOrderedValues() {
		List<List<T>> orderedValues = new ArrayList<List<T>>();
		for (Iterator<List<T>> iterator = this.iterator(); iterator.hasNext();) {
			orderedValues.add(iterator.next());
		}
		return orderedValues;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return new Iterator<List<T>>() {

			private List<T> returnedValues = new ArrayList<T>();

			@Override
			public boolean hasNext() {
				return returnedValues.size() < values.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<T> next() {
				List<T> unreturnedValues = ListUtils
						.subtract(new LinkedList<Object>(values.keySet()),
								returnedValues);
				List<T> returnValues = new ArrayList<T>();
				for (T element : unreturnedValues) {
					List<?> currentDependencies = ListUtils.subtract(
							values.get(element), returnedValues);
					if (currentDependencies.size() == 0)
						returnValues.add((T) element);
				}
				if (returnValues.size() > 0) {
					returnedValues.addAll(returnValues);
					return returnValues;
				} else {
					throw new RuntimeException(
							"No independent element could be found!");
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
