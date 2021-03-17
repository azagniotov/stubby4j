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

    // These fields are defined in various Stub* classes, e.g.: StubRequest or StubProxyConfig
    private static List<String> reflectionSkippableProperties =
            Collections.unmodifiableList(Arrays.asList("proxyConfigAsYAML", "regexGroups", "fileBytes"));

    private ReflectionUtils() {

    }

    @SuppressWarnings("deprecation")
    public static <T extends ReflectableStub> Map<String, String> getProperties(final T reflectable) throws IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
        final Map<String, String> properties = new HashMap<>();

        for (final Field field : reflectable.getClass().getDeclaredFields()) {

            AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return true;
            });

            if (reflectionSkippableProperties.contains(field.getName())) {
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

    @SuppressWarnings("deprecation")
    public static <T extends ReflectableStub> void injectObjectFields(final T reflectable, final Map<String, Object> fieldsAndValues) throws InvocationTargetException, IllegalAccessException {

        for (final Field field : reflectable.getClass().getDeclaredFields()) {
            final String fieldName = field.getName().toLowerCase(Locale.US);
            if (fieldsAndValues.containsKey(fieldName)) {
                AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return true;
                });

                field.set(reflectable, fieldsAndValues.get(fieldName));
            }
        }
    }

    public static Object getPropertyValue(final Object object, final String fieldName) throws InvocationTargetException, IllegalAccessException {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            // e.g.: "responseAsYAML" => StubHttpLifecycle.getResponseAsYAML
            if (method.getName().equalsIgnoreCase("get" + fieldName)) {
                return method.invoke(object);
            }
        }
        return null;
    }
}
