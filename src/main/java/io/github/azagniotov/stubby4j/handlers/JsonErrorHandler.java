package io.github.azagniotov.stubby4j.handlers;

import io.github.azagniotov.stubby4j.annotations.GeneratedCodeCoverageExclusion;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.utils.HandlerUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static io.github.azagniotov.stubby4j.common.Common.HEADER_X_STUBBY_HTTP_ERROR_REAL_REASON;

@GeneratedCodeCoverageExclusion
public class JsonErrorHandler extends ErrorHandler {

    private static final int BYTE_ARRAY_CAPACITY = 4096;
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerUtils.class);

    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response) throws IOException {

        baseRequest.setHandled(true);
        response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());

        try (final ByteArrayISO8859Writer byteArrayWriter = new ByteArrayISO8859Writer(BYTE_ARRAY_CAPACITY)) {
            final String realReason = response.getHeader(HEADER_X_STUBBY_HTTP_ERROR_REAL_REASON);

            handleErrorPage(request, byteArrayWriter, response.getStatus(), realReason);
            byteArrayWriter.flush();
            response.setContentLength(byteArrayWriter.size());
            byteArrayWriter.writeTo(response.getOutputStream());
            byteArrayWriter.destroy();
        }
    }

    // Issues with errorPageForMethod in Jetty versions > 9.4.20, see base method in ErrorHandler#errorPageForMethod
    // The Spring Framework people complaining: https://github.com/spring-projects/spring-boot/issues/18494
    @Override
    public boolean errorPageForMethod(final String method) {
        final HttpMethod httpMethod = HttpMethod.fromString(method);
        switch (httpMethod) {
            case GET:
            case POST:
            case PUT:
            case DELETE:
                return true;
            default:
                final String message = String.format("Skipped generating error JSON for method %s", httpMethod.asString());
                ANSITerminal.error(message);
                LOGGER.error(message);
                return false;
        }
    }

    @Override
    protected void writeErrorPage(final HttpServletRequest request,
                                  final Writer writer,
                                  final int code,
                                  final String realReason,
                                  final boolean showStacks) throws IOException {
        writer.write("{" +
                "\"code\":\"" + code + "\"," +
                "\"message\":\"" + HttpStatus.getMessage(code) + "\"}");
//        writer.write("{" +
//                "\"code\":\"" + code + "\"," +
//                "\"message\":\"" + HttpStatus.getMessage(code) + "\"," +
//                "\"reason\":" + realReason + "}");
    }
}
