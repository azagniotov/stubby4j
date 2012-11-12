package by.stub.testing.junit.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import unit.by.stub.cli.CommandLineIntepreterTest;
import unit.by.stub.handlers.StubsHandlerTest;
import unit.by.stub.handlers.strategy.DefaultResponseHandlingStrategyTest;
import unit.by.stub.handlers.strategy.HandlingStrategyFactoryTest;
import unit.by.stub.handlers.strategy.RedirectResponseHandlingStrategyTest;
import unit.by.stub.utils.CollectionUtilsTest;
import unit.by.stub.utils.HandlerUtilsTest;
import unit.by.stub.utils.ReflectionUtilsTest;
import unit.by.stub.yaml.stubs.StubResponseTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
      CommandLineIntepreterTest.class,
      StubsHandlerTest.class,
      DefaultResponseHandlingStrategyTest.class,
      HandlingStrategyFactoryTest.class,
      RedirectResponseHandlingStrategyTest.class,
      CollectionUtilsTest.class,
      HandlerUtilsTest.class,
      ReflectionUtilsTest.class,
      StubResponseTest.class
})
public class UnitTestSuite {
}
