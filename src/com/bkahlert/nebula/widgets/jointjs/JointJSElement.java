package com.bkahlert.nebula.widgets.jointjs;

import java.util.HashMap;

public class JointJSElement extends JointJSCell {

	private String title;

	public JointJSElement(HashMap<String, Object> cell) {
		super(cell);
	}

	public String getTitle() {
		if (this.title == null) {
			Object title = this.cell.get("title");
			if (title != null) {
				this.title = title.toString();
			}
		}
		return this.title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((this.getId() == null) ? 0 : this.getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		JointJSLink other = (JointJSLink) obj;
		if (this.getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!this.getId().equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Element \"" + this.getTitle() + "\"";
	}

}
