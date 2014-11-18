package com.bkahlert.nebula.widgets.jointjs;

import java.util.HashMap;

public class JointJSCell {

	protected final HashMap<String, Object> cell;

	private String id;

	public JointJSCell(HashMap<String, Object> cell) {
		this.cell = cell;

		Object id = this.cell.get("id");
		if (id != null) {
			this.id = id.toString();
		}
	}

	public String getId() {
		return this.id;
	}

	public Object getAttribute(String key) {
		return this.cell.get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.cell == null) ? 0 : this.cell.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
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
		JointJSCell other = (JointJSCell) obj;
		if (this.cell == null) {
			if (other.cell != null) {
				return false;
			}
		} else if (!this.cell.equals(other.cell)) {
			return false;
		}
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Cell \"" + this.getId() + "\"";
	}

}
