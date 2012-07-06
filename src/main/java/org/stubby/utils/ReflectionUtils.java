/*
A Java-based HTTP stub server

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

package org.stubby.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:10 AM
 */
public final class ReflectionUtils {

   public static Map<String, String> getProperties(final Object object) throws IllegalAccessException {
      final Map<String, String> properties = new HashMap<String, String>();

      for (final Field field : object.getClass().getDeclaredFields()) {
         field.setAccessible(true);
         final String value = (field.get(object) != null ? field.get(object).toString() : "Not provided");
         properties.put(field.getName().toLowerCase(), value);
      }

      return properties;
   }

   public static boolean isFieldCorrespondsToYamlNode(final Class<?> clazzor, final String fieldName) {
      for (final Field field : clazzor.getDeclaredFields()) {
         if (!fieldName.equals("headers") && field.getName().equalsIgnoreCase(fieldName)) {
            return true;
         }
      }
      return false;
   }

   public static void setValue(final Object object, final String fieldName, final String value) throws InvocationTargetException, IllegalAccessException {
      for (final Method method : object.getClass().getDeclaredMethods()) {
         if (method.getName().equalsIgnoreCase("set" + fieldName)) {
            method.invoke(object, value);
            break;
         }
      }
   }
}
