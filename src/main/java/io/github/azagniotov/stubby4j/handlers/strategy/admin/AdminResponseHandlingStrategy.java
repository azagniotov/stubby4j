package io.github.azagniotov.stubby4j.handlers.strategy.admin;

import io.github.azagniotov.stubby4j.stubs.StubRepository;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import static io.github.azagniotov.stubby4j.utils.StringUtils.getBytesUtf8;

public interface AdminResponseHandlingStrategy {

    Logger LOGGER = LoggerFactory.getLogger(AdminResponseHandlingStrategy.class);
    Pattern REGEX_PROXY_CONFIG = Pattern.compile("^(proxy-config)$");

    void handle(final HttpServletRequest request, final HttpServletResponse response, final StubRepository stubRepository) throws Exception;

    default void writeResponseOutputStream(final HttpServletResponse response, final String payload) {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(HttpStatus.OK_200);
        try (final OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(getBytesUtf8(payload));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Error writing to output stream: ", e);
        }
    }

    default String[] splitRequestURI(final HttpServletRequest request) {
        // e.g.: http://localhost:8889/<NUMERIC_ID>
        // e.g.: http://localhost:8889/<ALPHA_NUMERIC_UUID_STRING>
        // e.g.: http://localhost:8889/proxy-config/<ALPHA_NUMERIC_UUID_STRING>
        return Arrays.stream(request.getRequestURI().split("/"))
                .filter(uriPath -> !uriPath.trim().isEmpty())
                .map(String::trim)
                .toArray(String[]::new);
    }
}
