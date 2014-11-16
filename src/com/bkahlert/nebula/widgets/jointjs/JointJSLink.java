package com.bkahlert.nebula.widgets.jointjs;

import java.util.HashMap;
import java.util.List;

public class JointJSLink extends JointJSCell {

	public static interface IEndpoint {
	}

	public static interface ICoordinateEndpoint extends IEndpoint {
		public long getX();

		public long getY();
	}

	public static interface IElementEndpoint extends IEndpoint {
		public String getElement();
	}

	public static class CoordinateEndpoint implements ICoordinateEndpoint {
		private final int x;
		private final int y;

		public CoordinateEndpoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public long getX() {
			return this.x;
		}

		@Override
		public long getY() {
			return this.y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.x;
			result = prime * result + this.y;
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
			CoordinateEndpoint other = (CoordinateEndpoint) obj;
			if (this.x != other.x) {
				return false;
			}
			if (this.y != other.y) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "{ " + this.x + ", " + this.y + " }";
		}
	}

	public static class ElementEndpoint implements IElementEndpoint {
		private final String id;

		public ElementEndpoint(String id) {
			this.id = id;
		}

		@Override
		public String getElement() {
			return this.id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((this.id == null) ? 0 : this.id.hashCode());
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
			ElementEndpoint other = (ElementEndpoint) obj;
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
			return "{ " + this.id + " }";
		}
	}

	private String title;
	private IEndpoint source;
	private IEndpoint target;

	public JointJSLink(HashMap<String, Object> cell) {
		super(cell);
	}

	@SuppressWarnings("unchecked")
	public String getTitle() {
		if (this.title == null) {
			List<HashMap<String, Object>> labels = (List<HashMap<String, Object>>) this.cell
					.get("labels");
			if (labels != null) {
				for (HashMap<String, Object> label : labels) {
					this.title = (String) ((HashMap<String, Object>) ((HashMap<String, Object>) label
							.get("attrs")).get("text")).get("text");
				}
			}
		}
		return this.title;
	}

	public boolean isPermanent() {
		Object raw = this.cell.get("permanent");
		if (raw instanceof Boolean) {
			return (boolean) raw;
		} else {
			return false;
		}
	}

	private IEndpoint createEndpoint(HashMap<String, Object> endpoint) {
		if (endpoint.get("id") != null) {
			final String id = endpoint.get("id").toString();
			return new ElementEndpoint(id.toString());
		} else {
			final int x = Integer.valueOf(endpoint.get("x").toString());
			final int y = Integer.valueOf(endpoint.get("y").toString());
			return new CoordinateEndpoint(x, y);
		}
	}

	public IEndpoint getSource() {
		if (this.source == null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> source = (HashMap<String, Object>) this.cell
					.get("source");
			this.source = this.createEndpoint(source);
		}
		return this.source;
	}

	public IEndpoint getTarget() {
		if (this.target == null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> source = (HashMap<String, Object>) this.cell
					.get("target");
			this.target = this.createEndpoint(source);
		}
		return this.target;
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
		return "Link \"" + this.getTitle() + "\": " + this.getSource() + " -> "
				+ this.getTarget();
	}

}
