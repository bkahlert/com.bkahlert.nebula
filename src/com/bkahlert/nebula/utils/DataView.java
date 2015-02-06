package com.bkahlert.nebula.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;

/**
 * Instances of this class implement a specific view on a data structure.
 * <p>
 * A dependency is considered dirty if the hash code has changed.
 * <p>
 * A {@link DataView} instance can serve as a dependency, too.
 *
 * @author bkahlert
 *
 */
public abstract class DataView extends Dirtiable implements IDirtiable {

	private static final Logger LOGGER = Logger.getLogger(DataView.class);

	private long lastModification;
	// we use a list to not rely on the objects hashes
	private List<Pair<IDirtiable, Long>> dependencyLastModification;

	/**
	 * <b>Important:</b> Be sure to use {@link System#nanoTime()} for the
	 * modification date. Otherwise the dirty check mechanism might interpret
	 * something as non-dirty because of the small resolution of
	 * {@link System#currentTimeMillis()}.
	 *
	 * @param dependency
	 */
	public DataView(IDirtiable dependency) {
		this(dependency, new IDirtiable[] {});
	}

	/**
	 * <b>Important:</b> Be sure to use {@link System#nanoTime()} for the
	 * modification date. Otherwise the dirty check mechanism might interpret
	 * something as non-dirty because of the small resolution of
	 * {@link System#currentTimeMillis()}.
	 *
	 * @param dependencies
	 */
	public DataView(IDirtiable dependency, IDirtiable... dependencies) {
		Assert.isLegal(dependency != null);
		this.dependencyLastModification = new ArrayList<>();
		this.dependencyLastModification.add(new Pair<>(dependency, null));
		for (IDirtiable d : dependencies) {
			this.dependencyLastModification.add(new Pair<>(d, null));
		}
		this.modified();
	}

	@Override
	public void modified() {
		this.lastModification = System.nanoTime();
	}

	@Override
	public long getLastModification() {
		return this.lastModification;
	}

	protected abstract void refresh();

	synchronized protected void checkAndRefresh() {
		List<Pair<IDirtiable, Long>> updatedDependencyHashValue = new ArrayList<>(
				this.dependencyLastModification.size());
		for (Iterator<Pair<IDirtiable, Long>> iterator = this.dependencyLastModification
				.iterator(); iterator.hasNext();) {
			Pair<IDirtiable, Long> dependencyHashValue = iterator.next();
			IDirtiable dependency = dependencyHashValue.getFirst();
			Long upToDateUntil = dependencyHashValue.getSecond();
			long lastModification = dependency.getLastModification();

			if (upToDateUntil == null || upToDateUntil < lastModification) {
				iterator.remove();
				updatedDependencyHashValue.add(new Pair<IDirtiable, Long>(
						dependency, lastModification));
			}
		}

		if (updatedDependencyHashValue.size() > 0) {
			this.dependencyLastModification.addAll(updatedDependencyHashValue);
			try {
				LOGGER.warn("Starting Refresh: " + this.getClass().toString());
				this.refresh();
				LOGGER.warn("Finished Refresh: " + this.getClass().toString());
			} catch (Exception e) {
				LOGGER.error("Failed Refresh " + this.getClass().toString(), e);
			} finally {
				this.modified();
			}
		}
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((this.dependencyLastModification == null) ? 0
						: this.dependencyLastModification.hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		DataView other = (DataView) obj;
		if (this.dependencyLastModification == null) {
			if (other.dependencyLastModification != null) {
				return false;
			}
		} else if (!this.dependencyLastModification
				.equals(other.dependencyLastModification)) {
			return false;
		}
		return true;
	}

}
