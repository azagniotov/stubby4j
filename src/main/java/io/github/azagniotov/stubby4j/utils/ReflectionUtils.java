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

package io.github.azagniotov.stubby4j.utils;

import io.github.azagniotov.stubby4j.stubs.ReflectableStub;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class ReflectionUtils {

    private static List<String> skipableProperties =
            Collections.unmodifiableList(Arrays.asList("STUBBY_RESOURCE_ID_HEADER", "regexGroups", "fileBytes"));

    private ReflectionUtils() {

    }

    public static <T extends ReflectableStub> Map<String, String> getProperties(final T reflectable) throws IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
        final Map<String, String> properties = new HashMap<>();

        for (final Field field : reflectable.getClass().getDeclaredFields()) {

            AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return true;
                }
            });

            if (skipableProperties.contains(field.getName())) {
                continue;
            }

            final Object fieldObject = ReflectionUtils.getPropertyValue(reflectable, field.getName());
            final String value = StringUtils.objectToString(fieldObject);

            if (!value.equals(StringUtils.NOT_PROVIDED) && !value.equals("{}")) {
                properties.put(StringUtils.toLower(field.getName()), value);
            }
        }

        return properties;
    }

    public static <T extends ReflectableStub> void injectObjectFields(final T reflectable, final String fieldName, final Object value) throws InvocationTargetException, IllegalAccessException {
        final Map<String, Object> fieldAndValue = new HashMap<>();
        fieldAndValue.put(fieldName.toLowerCase(Locale.US), value);
        injectObjectFields(reflectable, fieldAndValue);
    }

    public static <T extends ReflectableStub> void injectObjectFields(final T reflectable, final Map<String, Object> fieldsAndValues) throws InvocationTargetException, IllegalAccessException {

        for (final Field field : reflectable.getClass().getDeclaredFields()) {
            final String fieldName = field.getName().toLowerCase(Locale.US);
            if (fieldsAndValues.containsKey(fieldName)) {
                AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        return true;
                    }
                });

                field.set(reflectable, fieldsAndValues.get(fieldName));
            }
        }
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
