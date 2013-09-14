package by.stub.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 6:33 PM
 */
public final class JarUtils {

   private JarUtils() {

   }

   public static String readManifestImplementationVersion() {
      final URLClassLoader classLoader = (URLClassLoader) JarUtils.class.getClassLoader();
      try {
         final URL url = classLoader.findResource("META-INF/MANIFEST.MF");
         final Manifest manifest = new Manifest(url.openStream());
         final String rawVersion = manifest.getMainAttributes().getValue("Implementation-Version");
         return String.format("%s", rawVersion);
      } catch (Exception e) {
         //Do nothing
      }

      return String.format("%s", "x.x.xx");
   }
}
