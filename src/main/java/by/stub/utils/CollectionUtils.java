package by.stub.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Zagniotov
 * @since 11/6/12, 6:33 PM
 */
public final class CollectionUtils {

   private CollectionUtils() {

   }

   public static Map<String, String> constructParamMap(final String queryString) {

      if (!StringUtils.isSet(queryString))
         return new HashMap<String, String>();

      final Map<String, String> paramMap = new HashMap<String, String>();
      final String[] pairs = queryString.split("&");
      for (final String pair : pairs) {
         final String[] splittedPair = pair.split("=");
         paramMap.put(splittedPair[0], splittedPair[1]);
      }

      return paramMap;
   }

   public static String constructQueryString(final Map<String, String> query) {

      final Set<Map.Entry<String, String>> mapEntries = query.entrySet();
      final Iterator<Map.Entry<String, String>> iterator = mapEntries.iterator();
      final StringBuilder queryStringBuilder = new StringBuilder();

      while (iterator.hasNext()) {
         final Map.Entry<String, String> entry = iterator.next();
         final String pair = String.format("%s=%s", entry.getKey(), entry.getValue());

         queryStringBuilder.append(pair);
         if (iterator.hasNext()) {
            queryStringBuilder.append("&");
         }
      }

      return queryStringBuilder.toString();
   }
}
