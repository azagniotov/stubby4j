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
