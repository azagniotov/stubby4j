package by.stub.testing.junit.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Alexander Zagniotov
 * @since 11/11/12, 11:14 AM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
      UnitTestSuite.class,
      FunctionalTestSuite.class,
      IntegrationTestSuite.class,
      SystemTestSuite.class
})
public class AllSuiteRunner {
}
