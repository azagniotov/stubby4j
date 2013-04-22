/*
HTTP stub server written in Java with embedded Jetty

Copyright (C) 2012 Alexander Zagniotov, Isa Goksu and Eric Mrak

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.stub.utils;

import by.stub.cli.ANSITerminal;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Locale;

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

   public static void logOutgoingResponse(final HttpServletRequest request, final HttpServletResponseWithGetStatus wrapper, final String source) {

      final int status = wrapper.getStatus();

      final String logMessage = String.format("[%s] <- %s [%s]%s %s",
         getTime(),
         status,
         source,
         request.getRequestURI(),
         HttpStatus.getMessage(status)
      );

      if (status >= HttpStatus.BAD_REQUEST_400) {
         ANSITerminal.error(logMessage);
      } else if (status >= HttpStatus.MULTIPLE_CHOICES_300) {
         ANSITerminal.warn(logMessage);
      } else if (status >= HttpStatus.OK_200) {
         ANSITerminal.ok(logMessage);
      } else if (status >= HttpStatus.CONTINUE_100) {
         ANSITerminal.info(logMessage);
      } else {
         ANSITerminal.log(logMessage);
      }
   }

   private static String getTime() {
      final Calendar now = Calendar.getInstance(Locale.US);
      return String.format("%02d:%02d:%02d",
         now.get(Calendar.HOUR_OF_DAY),
         now.get(Calendar.MINUTE),
         now.get(Calendar.SECOND)
      );
   }
}
