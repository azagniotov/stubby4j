package org.stubby.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/19/12, 3:30 PM
 */
final class ResultSetConverter {

   private ResultSetConverter() {

   }

   static List<Map<String, Object>> convertResultSetToMap(final ResultSet resultSet) throws SQLException {
      final List<Map<String, Object>> rows = new LinkedList<Map<String, Object>>();
      while (resultSet.next()) {
         rows.add(handleRow(resultSet));
      }
      return rows;
   }

   private static Map<String, Object> handleRow(final ResultSet resultSet) throws SQLException {
      final Map<String, Object> map = new HashMap<String, Object>();
      final ResultSetMetaData metadata = resultSet.getMetaData();
      final int cols = metadata.getColumnCount();
      for (int i = 1; i <= cols; i++) {
         map.put(metadata.getColumnName(i), resultSet.getObject(i));
      }
      return map;
   }
}
