package com.bkahlert.nebula.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.bkahlert.nebula.utils.colors.AllTests.class,
		com.bkahlert.nebula.utils.history.AllTests.class,
		CalendarUtilsTest.class, CollectionUtilsTest.class,
		DateUtilsTest.class, DistributionUtilsTest.class, ExecUtilsTest.class,
		JSONUtilsTest.class, MathUtilsTest.class, OffWorkerTest.class,
		StringUtilsTest.class })
public class AllTests {

}
