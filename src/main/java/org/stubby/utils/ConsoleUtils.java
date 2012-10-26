package org.stubby.utils;

import org.eclipse.jetty.http.HttpStatus;
import org.stubby.cli.ANSITerminal;
import org.stubby.javax.servlet.http.HttpServletResponseWithGetStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 1:00 PM
 */
public final class ConsoleUtils {

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
   }

   public static void logIncomingRequest(final HttpServletRequest request, final String source) {

      final String logMessage = String.format("[%s] -> %s [%s]%s",
            getTime(),
            request.getMethod(),
            source,
            request.getRequestURI()
      );
      ANSITerminal.incoming(logMessage);
   }

   public static void logOutgoingResponse(final HttpServletRequest request, final HttpServletResponse response, final String source) {
      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);

      final int status = wrapper.getStatus();

      final String logMessage = String.format("[%s] <- %s [%s]%s %s",
            getTime(),
            status,
            source,
            request.getRequestURI(),
            HttpStatus.getMessage(status)
      );

      if (status >= 400 && status < 600)
         ANSITerminal.error(logMessage);
      else if (status >= 300)
         ANSITerminal.warn(logMessage);
      else if (status >= 200)
         ANSITerminal.ok(logMessage);
      else if (status >= 100)
         ANSITerminal.info(logMessage);
      else
         ANSITerminal.log(logMessage);
   }

   private static String getTime() {
      final Calendar now = Calendar.getInstance();
      return String.format("%02d:%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            now.get(Calendar.SECOND)
      );
   }
}
