package io.github.azagniotov.stubby4j.handlers.strategy.stubs;

import io.github.azagniotov.stubby4j.stubs.StubRequest;
import io.github.azagniotov.stubby4j.stubs.StubResponse;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.github.azagniotov.stubby4j.utils.StringUtils.isTokenized;
import static io.github.azagniotov.stubby4j.utils.StringUtils.replaceTokensInString;

public class RedirectResponseHandlingStrategy implements StubResponseHandlingStrategy {

    private final StubResponse foundStubResponse;

    RedirectResponseHandlingStrategy(final StubResponse foundStubResponse) {
        this.foundStubResponse = foundStubResponse;
    }

    @Override
    public void handle(final HttpServletResponse response, final StubRequest assertionStubRequest) throws Exception {
        HandlerUtils.setResponseMainHeaders(response);
        final Map<String, String> regexGroups = assertionStubRequest.getRegexGroups();

        if (StringUtils.isSet(foundStubResponse.getLatency())) {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
        }

        final String headerLocation = foundStubResponse.getHeaders().get("location");
        if (isTokenized(headerLocation)) {
            response.setHeader(HttpHeader.LOCATION.asString(), replaceTokensInString(headerLocation, regexGroups));
        } else {
            response.setHeader(HttpHeader.LOCATION.asString(), headerLocation);
        }

        response.setStatus(foundStubResponse.getHttpStatusCode().getCode());
        response.setHeader(HttpHeader.CONNECTION.asString(), "close");
    }
}
