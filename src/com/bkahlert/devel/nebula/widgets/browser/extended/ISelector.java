package com.bkahlert.devel.nebula.widgets.browser.extended;

public interface ISelector {

	/**
	 * Instances of this class simply use the given string as their selector.
	 * 
	 * @author bkahlert
	 * 
	 */
	public class CssSelector implements ISelector {
		private String expr;

		public CssSelector(String expr) {
			this.expr = expr;
		}

		@Override
		public String toString() {
			return this.expr;
		}
	}

	/**
	 * Instances of this class select the element with a certain ID.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class IdSelector extends CssSelector {
		public IdSelector(String id) {
			super("#" + id);
		}
	}

	/**
	 * Instances of this class select all elements with a certain name.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class NameSelector extends CssSelector {
		public NameSelector(String name) {
			super("*[name=" + name + "]");
		}
	}

	/**
	 * Instances of this class select all elements with a certain ID or name.
	 * 
	 * @return
	 */
	public static class FieldSelector extends CssSelector {

		public FieldSelector(String idOrName) {
			super("#" + idOrName + ",*[name=" + idOrName + "]");
		}

	}

	@Override
	public String toString();

}
