package org.stubby.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Alexander Zagniotov
 * @since 6/16/12, 6:24 PM
 */
final class Formatter {

   public static final String formatHealthCheckResults(final ResultSet resultSet, final String tableName, final String... columnNames) {

      final StringBuilder builder = new StringBuilder();
      try {
         final String template = columnNames.length == 4 ? "%-2s | %-18s | %-18s | %-18s" : "%-2s | %-18s | %-18s";
         builder.append(tableName).append("\n").append(String.format(template, columnNames)).append("\n");

         while (resultSet.next()) {

            final String[] params = new String[columnNames.length];
            for (int idx = 0; idx < columnNames.length; idx++) {
               final String columnValue = resultSet.getString(columnNames[idx]);
               params[idx] = columnValue;
            }

            builder.append(String.format(template, params));
            builder.append("\n");
         }

         if (!builder.toString().isEmpty()) {
            return builder.toString();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return "ERR: Could not get system healthcheck ..";
   }

   public static final String aggregateResultsIntoOneMessage(final boolean dbClosed, final String catalog, final String... queryResults) {
      final StringBuilder statusBuilder = new StringBuilder();

      statusBuilder.append("Is database connection opened: ");
      statusBuilder.append((dbClosed ? "NO" : "YES"));
      statusBuilder.append("\n");
      statusBuilder.append("Default database name: ");
      statusBuilder.append(catalog);
      statusBuilder.append("\n\n");

      for (String result : queryResults) {
         statusBuilder.append(result);
         statusBuilder.append("\n\n\n");
      }

      return statusBuilder.toString();
   }

}
