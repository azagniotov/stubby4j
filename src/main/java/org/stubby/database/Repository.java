package org.stubby.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stubby.yaml.stubs.StubHttpLifecycle;
import org.stubby.yaml.stubs.StubRequest;
import org.stubby.yaml.stubs.StubResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 4:45 PM
 */
public class Repository {

   private final static String DB_NAME = "ENDPOINTS";
   private final static Logger logger = LoggerFactory.getLogger(Repository.class);

   private static Connection dbConnection = null;
   private final List<StubHttpLifecycle> httpLifecycles;

   public Repository(final List<StubHttpLifecycle> httpLifecycles) throws ClassNotFoundException, SQLException {
      this.httpLifecycles = httpLifecycles;
      initConnection();
      createEndpointTable();
      insertTableData();
   }

   private void createEndpointTable() throws SQLException {
      final Statement st = dbConnection.createStatement();

      st.execute("CREATE TABLE ENDPOINTS.REQUEST " +
            "(ID INT PRIMARY KEY, " +
            "URL VARCHAR(255) DEFAULT NULL, " +
            "METHOD VARCHAR(255) DEFAULT NULL, " +
            "POSTBODY TEXT DEFAULT NULL) NOT PERSISTENT");

      st.execute("CREATE TABLE ENDPOINTS.REQUEST_HEADERS " +
            "(ID INT PRIMARY KEY, " +
            "PARAM VARCHAR(255) DEFAULT NULL, " +
            "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT");

      st.execute("CREATE TABLE ENDPOINTS.RESPONSE " +
            "(ID INT PRIMARY KEY, " +
            "STATUS VARCHAR(255) DEFAULT NULL, " +
            "BODY TEXT DEFAULT NULL) NOT PERSISTENT");

      st.execute("CREATE TABLE ENDPOINTS.RESPONSE_HEADERS " +
            "(ID INT PRIMARY KEY, " +
            "PARAM VARCHAR(255) DEFAULT NULL, " +
            "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT");

      st.close();
   }

   private void insertTableData() throws SQLException {

      for (int idx = 0; idx < httpLifecycles.size(); idx++) {
         final StubHttpLifecycle httpLifecycle = httpLifecycles.get(idx);

         final StubRequest request = httpLifecycle.getRequest();
         insertRequestData(idx, request);

         final StubResponse response = httpLifecycle.getResponse();
         insertResponseData(idx, response);
      }
   }

   private void insertRequestData(int index, final StubRequest request) throws SQLException {
      final String preparedSql = "INSERT INTO ENDPOINTS.REQUEST (ID, URL, METHOD, POSTBODY) VALUES (?, ?, ?, ?)";
      final PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedSql);
      preparedStatement.setInt(1, index);
      preparedStatement.setString(2, request.getUrl());
      preparedStatement.setString(3, request.getMethod());
      preparedStatement.setString(4, request.getPostBody());
      preparedStatement.executeUpdate();
      preparedStatement.close();
   }

   private void insertResponseData(int index, final StubResponse response) throws SQLException {
      final String preparedSql = "INSERT INTO ENDPOINTS.RESPONSE (ID, STATUS, BODY) VALUES (?, ?, ?)";
      final PreparedStatement preparedStatement = dbConnection.prepareStatement(preparedSql);
      preparedStatement.setInt(1, index);
      preparedStatement.setString(2, response.getStatus());
      preparedStatement.setString(3, response.getBody());
      preparedStatement.executeUpdate();
      preparedStatement.close();
   }

   private void initConnection() throws ClassNotFoundException, SQLException {
      Class.forName("org.h2.Driver");
      final String url = "jdbc:h2:mem:endpoints;";
      final String init = "INIT=CREATE SCHEMA IF NOT EXISTS ENDPOINTS\\;SET SCHEMA ENDPOINTS;";
      final String options = "DB_CLOSE_DELAY=-1\\;SET DEFAULT_TABLE_TYPE=MEMORY;";
      if (dbConnection == null) {
         dbConnection = DriverManager.getConnection(url + init + options, "sa", "");
         logger.info("Opened DB connection at " + url);
      }
   }

   public String getHealthCheck() {
      try {
         final boolean isDbClosed = dbConnection.isClosed();
         final String catalog = dbConnection.getCatalog();

         final StringBuilder statusBuilder = new StringBuilder();
         statusBuilder.append("Is database connection opened: ");
         statusBuilder.append((isDbClosed ? "NO" : "YES"));
         statusBuilder.append("\n");
         statusBuilder.append("Default database name: ");
         statusBuilder.append(catalog);
         statusBuilder.append("\n\n");

         final String requestQuery = "SELECT * FROM ENDPOINTS.REQUEST";
         final String requestQueryResult = executeStatusQuery(requestQuery, "ENDPOINTS.REQUEST", "ID", "URL", "METHOD", "POSTBODY");
         statusBuilder.append(requestQueryResult);
         statusBuilder.append("\n\n\n");

         final String responseQuery = "SELECT * FROM ENDPOINTS.RESPONSE";
         final String responseQueryResult = executeStatusQuery(responseQuery, "ENDPOINTS.RESPONSE", "ID", "STATUS", "BODY");
         statusBuilder.append(responseQueryResult);

         return statusBuilder.toString();

      } catch (SQLException e) {
         e.printStackTrace();
      }

      return null;
   }

   public final Map<String, String> findResponseFor(final String method, final String pathInfo) throws SQLException {
      final String requestPreparedSql = "SELECT ID FROM ENDPOINTS.REQUEST WHERE METHOD = ? AND URL = ?";
      final PreparedStatement requestPreparedStatement = dbConnection.prepareStatement(requestPreparedSql);
      requestPreparedStatement.setString(1, method);
      requestPreparedStatement.setString(2, pathInfo);

      int requestId = -1;
      final ResultSet requestSelectResultSet = requestPreparedStatement.executeQuery();
      while (requestSelectResultSet.next()) {
         requestId = requestSelectResultSet.getInt("ID");
      }
      requestPreparedStatement.close();

      final String responsePreparedSql = "SELECT STATUS, BODY FROM ENDPOINTS.RESPONSE WHERE ID = ?";
      final PreparedStatement responsePreparedStatement = dbConnection.prepareStatement(responsePreparedSql);
      responsePreparedStatement.setInt(1, requestId);
      final ResultSet responseSelectResultSet = responsePreparedStatement.executeQuery();
      String responseBody = "Nothing..";
      final Map<String, String> responseValues = new HashMap<String, String>();
      responseValues.put("crap", "No data found for " + method + " " + pathInfo);
      while (responseSelectResultSet.next()) {
         responseValues.put("status", responseSelectResultSet.getString("STATUS"));
         responseValues.put("body", responseSelectResultSet.getString("BODY"));
      }
      responsePreparedStatement.close();

      return responseValues;
   }

   public final String executeStatusQuery(final String rawSQL, final String tableName, String... columnNames) {

      final StringBuilder builder = new StringBuilder();

      try {

         final Statement statement = dbConnection.createStatement();
         final ResultSet resultSet = statement.executeQuery(rawSQL);

         builder.append(tableName).append(" ").append(Arrays.toString(columnNames).toString()).append("\n\n");

         while (resultSet.next()) {
            for (int idx = 0; idx < columnNames.length; idx++) {
               final String columnName = columnNames[idx];
               final String columnValue = resultSet.getString(columnName);
               builder.append(columnValue);
               if (idx + 1 < columnNames.length) {
                  builder.append("\t::\t");
               }
            }
            builder.append("\n");
         }
         statement.close();

         if (!builder.toString().isEmpty()) {
            return builder.toString();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return String.format("No results found in database for query [%s]", rawSQL);
   }
}