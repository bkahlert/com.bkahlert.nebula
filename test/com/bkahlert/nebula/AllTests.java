package com.bkahlert.nebula;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ com.bkahlert.nebula.data.AllTests.class,
		com.bkahlert.nebula.lang.AllTests.class,
		com.bkahlert.nebula.datetime.AllTests.class,
		com.bkahlert.nebula.rendering.AllTests.class,
		com.bkahlert.nebula.utils.AllTests.class,
		com.bkahlert.nebula.widgets.browser.AllTests.class, })
public class AllTests {

}
