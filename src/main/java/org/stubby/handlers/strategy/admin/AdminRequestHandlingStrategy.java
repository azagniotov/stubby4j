package org.stubby.handlers.strategy.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Zagniotov
 * @since 7/15/12, 10:46 AM
 */
public interface AdminRequestHandlingStrategy {
   void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException;
}
