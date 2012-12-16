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

package by.stub.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 10:10 AM
 */
@SuppressWarnings("serial")
public final class ReflectionUtils {

   private static List<String> skipableProperties = Collections.unmodifiableList(new ArrayList<String>() {{
      add("AUTH_HEADER");
   }});

   private ReflectionUtils() {

   }

   public static Map<String, String> getProperties(final Object object) throws IllegalAccessException, InvocationTargetException {
      final Map<String, String> properties = new HashMap<String, String>();

      for (final Field field : object.getClass().getDeclaredFields()) {

         AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
               field.setAccessible(true);
               return true;
            }
         });

         if (skipableProperties.contains(field.getName())) {
            continue;
         }

         //final Object fieldObject = field.get(object);
         final Object fieldObject = ReflectionUtils.getPropertyValue(object, field.getName());
         final String value = StringUtils.isObjectSet(fieldObject) ? fieldObject.toString() : "Not provided";
         properties.put(StringUtils.toLower(field.getName()), value);
      }

      return properties;
   }

   public static void setPropertyValue(final Object object, final String fieldName, final Object value) throws InvocationTargetException, IllegalAccessException {
      try {
         final Field field = object.getClass().getDeclaredField(fieldName);
         field.setAccessible(true);
         field.set(object, value);
      } catch (NoSuchFieldException ignored) { }
   }

   public static Object getPropertyValue(final Object object, final String fieldName) throws InvocationTargetException, IllegalAccessException {
      for (final Method method : object.getClass().getDeclaredMethods()) {
         if (method.getName().equalsIgnoreCase("get" + fieldName)) {
            return method.invoke(object);
         }
      }
      return null;
   }
}
