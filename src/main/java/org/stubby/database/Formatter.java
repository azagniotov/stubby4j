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
         builder.append(tableName).append("\n").append(String.format(template, (Object[]) columnNames)).append("\n");

         while (resultSet.next()) {

            final String[] params = new String[columnNames.length];
            for (int idx = 0; idx < columnNames.length; idx++) {
               final String columnValue = resultSet.getString(columnNames[idx]);
               params[idx] = columnValue;
            }

            builder.append(String.format(template, (Object[]) params));
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
