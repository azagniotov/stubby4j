package io.github.azagniotov.stubby4j.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;


public final class JarUtils {

    private JarUtils() {

    }


    public static String readManifestImplementationVersion() {
        final URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        try {
            final URL url = classLoader.findResource("META-INF/MANIFEST.MF");
            final Manifest manifest = new Manifest(url.openStream());

            return manifest.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            return "x.x.xx";
        }
    }


    public static String readManifestBuiltDate() {
        final URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();

        try {
            final URL url = classLoader.findResource("META-INF/MANIFEST.MF");
            final Manifest manifest = new Manifest(url.openStream());

            final String builtDate = manifest.getMainAttributes().getValue("Built-Date");
            if (builtDate != null) {
                return builtDate;
            }
        } catch (Exception e) {
            return DateTimeUtils.systemDefault();
        }

        return DateTimeUtils.systemDefault();
    }
}
