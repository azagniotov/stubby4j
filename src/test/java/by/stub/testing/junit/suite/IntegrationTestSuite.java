package by.stub.testing.junit.suite;

import integration.by.stub.database.DataStoreTest;
import integration.by.stub.database.thread.ConfigurationScannerTest;
import integration.by.stub.http.client.StubbyClientTest;
import integration.by.stub.utils.HandlerUtilsTest;
import integration.by.stub.yaml.stubs.StubRequestTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
      DataStoreTest.class,
      ConfigurationScannerTest.class,
      StubbyClientTest.class,
      HandlerUtilsTest.class,
      StubRequestTest.class
})
public class IntegrationTestSuite {
}
