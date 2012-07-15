package org.stubby.handlers.strategy;

import org.stubby.handlers.HttpRequestInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 10:46 AM
 */
public interface HandlingStrategy {
   void handle(final HttpServletResponse response, final HttpRequestInfo httpRequestInfo) throws IOException;
}
