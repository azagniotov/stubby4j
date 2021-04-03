package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubProxyConfig;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;

@GeneratedCodeCoverageExclusion
public final class ConsoleUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleUtils.class);
    private static final String DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP = " ***** [DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** ";
    private static final String DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP_END = " ***** [/DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** ";

    private static final String DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP = " ***** [DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** ";
    private static final String DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP_END = " ***** [/DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** ";

    private static boolean debug = false;

    private ConsoleUtils() {

    }


    public static void logIncomingRequestError(final HttpServletRequest request, final String source, final String error) {

        final String logMessage = String.format("[%s] -> %s [%s]%s: %s",
                getTime(),
                request.getMethod(),
                source,
                request.getRequestURI(),
                error
        );
        ANSITerminal.error(logMessage);
        LOGGER.error(logMessage);
    }


    private static void logRawIncomingRequest(final HttpServletRequest request) {
        ANSITerminal.warn(DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP);
        ANSITerminal.info(HttpRequestUtils.dump(request));
        ANSITerminal.warn(DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP_END + BR);

        LOGGER.debug(DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP);
        LOGGER.debug(HttpRequestUtils.dump(request));
        LOGGER.debug(DEBUG_INCOMING_RAW_HTTP_REQUEST_DUMP_END);
    }


    public static void logIncomingRequest(final HttpServletRequest request) {

        final String logMessage = String.format("[%s] -> %s [%s]",
                getTime(),
                request.getMethod(),
                request.getRequestURI()
        );
        ANSITerminal.incoming(logMessage);
        LOGGER.info(logMessage);

        if (debug) {

            final List<String> skipUriPrefix = new LinkedList<String>() {{
                add("/ajax");
                add("/status");
                add("/favicon");
            }};

            for (final String prefix : skipUriPrefix) {
                if (request.getRequestURI().startsWith(prefix)) {
                    return;
                }
            }

            ConsoleUtils.logRawIncomingRequest(request);
        }
    }


    public static void logAssertingRequest(final StubRequest assertingStubRequest) {
        if (debug) {
            ANSITerminal.warn(DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP);
            ANSITerminal.info(assertingStubRequest.toString());
            ANSITerminal.warn(DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP_END + BR);

            LOGGER.debug(DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP);
            LOGGER.debug("{}", assertingStubRequest);
            LOGGER.debug(DEBUG_INCOMING_ASSERTING_HTTP_REQUEST_DUMP_END);
        }
    }


    public static void logOutgoingResponse(final String url, final HttpServletResponse response) {
        final int status = response.getStatus();

        final String logMessage = String.format("[%s] <- %s [%s] %s",
                getTime(),
                status,
                url,
                HttpStatus.getMessage(status)
        );

        if (HttpStatus.isServerError(status) || HttpStatus.isClientError(status)) {
            ANSITerminal.error(logMessage);
            LOGGER.error(logMessage);
        } else if (HttpStatus.isRedirection(status)) {
            ANSITerminal.warn(logMessage);
            LOGGER.warn(logMessage);
        } else if (HttpStatus.isSuccess(status)) {
            ANSITerminal.ok(logMessage);
            LOGGER.info(logMessage);
        } else if (HttpStatus.isInformational(status)) {
            ANSITerminal.info(logMessage);
            LOGGER.info(logMessage);
        } else {
            ANSITerminal.log(logMessage);
            LOGGER.debug(logMessage);
        }
    }

    public static void logUnmarshalledProxyConfig(final StubProxyConfig stubProxyConfig) {
        final StringBuilder loadedMsgBuilder = new StringBuilder("Loaded proxy config metadata: ")
                .append(stubProxyConfig.getUUID());
        if (isSet(stubProxyConfig.getDescription())) {
            loadedMsgBuilder.append(String.format(" [%s]", stubProxyConfig.getDescription()));
        }

        final String logMessage = loadedMsgBuilder.toString();
        ANSITerminal.loaded(logMessage);
        LOGGER.info(logMessage);
    }

    public static void logUnmarshalledStub(final StubHttpLifecycle lifecycle) {
        final StubRequest request = lifecycle.getRequest();

        final StringBuilder loadedMsgBuilder = new StringBuilder("Loaded stub metadata: ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getUrl());
        if (isSet(lifecycle.getDescription())) {
            loadedMsgBuilder.append(String.format(" [%s]", lifecycle.getDescription()));
        }

        final String logMessage = loadedMsgBuilder.toString();
        ANSITerminal.loaded(logMessage);
        LOGGER.info(logMessage);
    }


    public static String getTime() {
        final Calendar now = Calendar.getInstance(Locale.US);
        return String.format("%02d:%02d:%02d",
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND)
        );
    }

    /**
     * Enables verbose console output
     *
     * @param isDebug if true, the incoming raw HTTP request will be dumped to console
     */

    public static void enableDebug(final boolean isDebug) {
        debug = isDebug;
    }
}
