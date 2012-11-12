package by.stub.testing.junit.suite;

import functional.by.stub.server.JettyManagerFactoryTest;
import functional.by.stub.yaml.YamlParserTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Alexander Zagniotov
 * @since 11/11/12, 11:53 AM
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
      YamlParserTest.class,
      JettyManagerFactoryTest.class
})
public class FunctionalTestSuite {
}
