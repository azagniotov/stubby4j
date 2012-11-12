package by.stub.testing.junit.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import system.by.stub.StubsTest;
import system.by.stub.AdminTest;

/**
 * @author Alexander Zagniotov
 * @since 11/11/12, 11:53 AM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
      StubsTest.class,
      AdminTest.class
})
public class SystemTestSuite {
}
