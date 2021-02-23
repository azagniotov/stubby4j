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

/**
 * @author Alexander Zagniotov
 * @since 10/27/12, 12:09 AM
 */
public final class ObjectUtils {

   private ObjectUtils() {

   }

   public static boolean isNull(final Object instance) {
      return instance == null;
   }

   public static boolean isNotNull(final Object instance) {
      return !isNull(instance);
   }
}
