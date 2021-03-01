package io.github.azagniotov.stubby4j.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;


public final class JarUtils {

    private JarUtils() {

    }

    public static String readManifestImplementationVersion() {
        try {
            final Manifest manifest = getManifest();

            return manifest.getMainAttributes().getValue("Implementation-Version");
        } catch (Exception e) {
            return "x.x.xx";
        }
    }


    public static String readManifestBuiltDate() {
        try {
            final Manifest manifest = getManifest();

            final String builtDate = manifest.getMainAttributes().getValue("Built-Date");
            if (builtDate != null) {
                return builtDate;
            }
        } catch (Exception e) {
            return DateTimeUtils.systemDefault();
        }

        return DateTimeUtils.systemDefault();
    }

    private static Manifest getManifest() throws IOException {

        final InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("META-INF/MANIFEST.MF");

        return new Manifest(inputStream);
    }
}
