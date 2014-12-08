package by.stub.handlers.strategy.stubs.callback;

import by.stub.yaml.stubs.StubCallback;

public class StubsCallbackHandlingStrategyFactory {

	private StubsCallbackHandlingStrategyFactory() {

	}

	public static StubCallbackHandlingStrategy getStrategy(
			final StubCallback foundStubCallback) {
		return new DefaultCallbackHandlingStrategy();
	}
}
