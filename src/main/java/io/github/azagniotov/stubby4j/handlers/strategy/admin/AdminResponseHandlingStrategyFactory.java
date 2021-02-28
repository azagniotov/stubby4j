package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import javax.servlet.http.HttpServletRequest;

public final class AdminResponseHandlingStrategyFactory {

    private AdminResponseHandlingStrategyFactory() {

    }

    public static AdminResponseHandlingStrategy getStrategy(final HttpServletRequest request) {

        final String method = request.getMethod();
        final HttpVerbsEnum verbEnum;

        try {
            verbEnum = HttpVerbsEnum.valueOf(method);
        } catch (final IllegalArgumentException ex) {
            return new NullHandlingStrategy();
        }

        switch (verbEnum) {
            case POST:
                return new PostHandlingStrategy();

            case PUT:
                return new PutHandlingStrategy();

            case DELETE:
                return new DeleteHandlingStrategy();

            default:
                return new GetHandlingStrategy();
        }
    }
}
