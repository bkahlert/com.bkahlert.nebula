package com.bkahlert.nebula.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;

/**
 * This {@link PropertyTester} can be used to debug Eclipse Core Expressions.
 * Simply test the object using the property
 * <code>com.bkahlert.nebula.debug</code> or a the reference
 * <code>com.bkahlert.nebula.debug</code>.
 * 
 * @author bkahlert
 * 
 */
public class ExpressionDebugger extends PropertyTester {

	private static final Logger LOGGER = Logger
			.getLogger(ExpressionDebugger.class);

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		String argsString;
		if (args == null) {
			argsString = "null";
		}
		if (args.length == 0) {
			argsString = "[]";
		} else {
			argsString = "[" + StringUtils.join(args, ", ") + "]";
		}
		LOGGER.debug("Evaluation:\n\tReceiver: " + receiver + "\n\tProperty: "
				+ property + "\n\tArguments: " + argsString
				+ "\n\tExpected Value: " + expectedValue);

		return true;
	}

}
