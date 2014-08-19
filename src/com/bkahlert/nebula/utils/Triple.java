package com.bkahlert.nebula.utils;

public class Triple<F, S, T> {
	private final F first;
	private final S second;
	private final T third;

	public Triple(F first, S second, T third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public F getFirst() {
		return this.first;
	}

	public S getSecond() {
		return this.second;
	}

	public T getThird() {
		return this.third;
	}

	@Override
	public String toString() {
		return "{" + this.getFirst() + ", " + this.getSecond() + ", "
				+ this.getThird() + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.first == null) ? 0 : this.first.hashCode());
		result = prime * result
				+ ((this.second == null) ? 0 : this.second.hashCode());
		result = prime * result
				+ ((this.third == null) ? 0 : this.third.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Triple other = (Triple) obj;
		if (this.first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!this.first.equals(other.first)) {
			return false;
		}
		if (this.second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!this.second.equals(other.second)) {
			return false;
		}
		if (this.third == null) {
			if (other.third != null) {
				return false;
			}
		} else if (!this.third.equals(other.third)) {
			return false;
		}
		return true;
	}

}