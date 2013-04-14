package by.stub.utils;

import java.util.Arrays;
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

         final String queryValue = splittedPair[1];
         if (queryValue.startsWith("%5B") && queryValue.endsWith("%5D")) {
            final String[] arrayParams = queryValue.
               replaceAll("%5B", "").
               replaceAll("%5D", "").
               replaceAll("%22", "\"").split(",");

            final String listWithParams = Arrays.asList(arrayParams).toString();
            paramMap.put(splittedPair[0], listWithParams);
            continue;
         }

         paramMap.put(splittedPair[0], queryValue);
      }

      return paramMap;
   }

   public static String constructQueryString(final Map<String, String> query) {

      final Set<Map.Entry<String, String>> mapEntries = query.entrySet();
      final Iterator<Map.Entry<String, String>> iterator = mapEntries.iterator();
      final StringBuilder queryStringBuilder = new StringBuilder();

      while (iterator.hasNext()) {
         final Map.Entry<String, String> entry = iterator.next();
         Object entryValue = entry.getValue();

         final String pair = String.format("%s=%s", entry.getKey(), entryValue);

         queryStringBuilder.append(pair);
         if (iterator.hasNext()) {
            queryStringBuilder.append("&");
         }

      }

      return queryStringBuilder.toString();
   }
}
