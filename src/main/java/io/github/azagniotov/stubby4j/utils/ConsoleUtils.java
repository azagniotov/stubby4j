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

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.annotations.CoberturaIgnore;
import io.github.azagniotov.stubby4j.cli.ANSITerminal;
import io.github.azagniotov.stubby4j.stubs.StubHttpLifecycle;
import io.github.azagniotov.stubby4j.stubs.StubRequest;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Locale;

import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;
import static io.github.azagniotov.stubby4j.utils.StringUtils.isSet;

/**
 * @author Alexander Zagniotov
 * @since 10/26/12, 1:00 PM
 */
public final class ConsoleUtils {

   private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleUtils.class);

   private static boolean debug = false;

   private ConsoleUtils() {

   }

   @CoberturaIgnore
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

   @CoberturaIgnore
   private static void logRawIncomingRequest(final HttpServletRequest request) {
      ANSITerminal.warn(" ***** [DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** ");
      ANSITerminal.info(HttpRequestUtils.dump(request));
      ANSITerminal.warn(" ***** [DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** " + BR);
      LOGGER.debug(" ***** [DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** ");
      LOGGER.debug(HttpRequestUtils.dump(request));
      LOGGER.debug(" ***** [DEBUG INCOMING RAW HTTP REQUEST DUMP] ***** ");
   }

   @CoberturaIgnore
   public static void logIncomingRequest(final HttpServletRequest request) {

      final String logMessage = String.format("[%s] -> %s [%s]",
         getTime(),
         request.getMethod(),
         request.getRequestURI()
      );
      ANSITerminal.incoming(logMessage);

      if (debug) {
         ConsoleUtils.logRawIncomingRequest(request);
      }
   }

   @CoberturaIgnore
   public static void logAssertingRequest(final StubRequest assertingStubRequest) {
      if (debug) {
         ANSITerminal.warn(" ***** [DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** ");
         ANSITerminal.info(assertingStubRequest.toString());
         ANSITerminal.warn(" ***** [DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** " + BR);
         LOGGER.debug(" ***** [DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** ");
         LOGGER.debug("{}", assertingStubRequest);
         LOGGER.debug(" ***** [DEBUG INCOMING ASSERTING HTTP REQUEST DUMP] ***** ");
      }
   }

   @CoberturaIgnore
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

   @CoberturaIgnore
   public static void logUnmarshalledStub(final StubHttpLifecycle lifecycle) {
      final StubRequest request = lifecycle.getRequest();

      final StringBuilder loadedMsgBuilder = new StringBuilder("Loaded: ")
         .append(request.getMethod())
         .append(" ")
         .append(request.getUrl());
      if (isSet(lifecycle.getDescription())) {
         loadedMsgBuilder.append(String.format(" [%s]", lifecycle.getDescription()));
      }

      ANSITerminal.loaded(loadedMsgBuilder.toString());
   }

   @CoberturaIgnore
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
   @CoberturaIgnore
   public static void enableDebug(final boolean isDebug) {
      debug = isDebug;
   }
}
