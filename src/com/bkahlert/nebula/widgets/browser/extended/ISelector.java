package com.bkahlert.nebula.widgets.browser.extended;

import org.apache.commons.lang.StringUtils;

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
		private String id;

		public IdSelector(String id) {
			super("#" + id);
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

	/**
	 * Instances of this class select all elements with a certain name.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class NameSelector extends CssSelector {
		private String name;

		public NameSelector(String name) {
			super("*[name=" + name + "]");
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Instances of this class match all the provided {@link ISelector}s.
	 * 
	 * @author bkahlert
	 * 
	 */
	public static class OrSelector extends CssSelector {
		public OrSelector(ISelector... selectors) {
			super(StringUtils.join(selectors, ","));
		}
	}

	/**
	 * Instances of this class select all elements with a certain ID or name.
	 * 
	 * @return
	 */
	public static class FieldSelector extends OrSelector {

		public FieldSelector(String idOrName) {
			super(new IdSelector(idOrName), new NameSelector(idOrName));
		}

	}

	@Override
	public String toString();

}
