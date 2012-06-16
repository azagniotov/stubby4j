package org.stubby.database;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 4:45 PM
 */
public class Repository {

   private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

   private final static String DB_NAME = "ENDPOINTS";
   private final static String TBL_NAME_REQ = String.format("%s.REQUEST", DB_NAME);
   private final static String TBL_NAME_RES = String.format("%s.RESPONSE", DB_NAME);
   private final static String TBL_NAME_REQ_HEADERS = String.format("%s_HEADERS", TBL_NAME_REQ);
   private final static String TBL_NAME_RES_HEADERS = String.format("%s_HEADERS", TBL_NAME_RES);
   private final static String TBL_COLUMN_ID = "ID";
   private final static String TBL_COLUMN_STATUS = "STATUS";
   private final static String TBL_COLUMN_BODY = "BODY";
   private final static String TBL_COLUMN_POSTBODY = "POSTBODY";
   private final static String TBL_COLUMN_URL = "URL";
   private final static String TBL_COLUMN_METHOD = "METHOD";
   private final static String TBL_COLUMN_RESPONSE_ID = "RESPONSE_ID";
   private final static String TBL_COLUMN_REQUEST_ID = "REQUEST_ID";
   private static final String TBL_COLUMN_PARAM = "PARAM";
   private static final String TBL_COLUMN_VALUE = "VALUE";

   private static Connection dbConnection = null;

   public Repository(final List<StubHttpLifecycle> httpLifecycles) {
      initConnection();
      createEndpointTables();
      insertTableData(httpLifecycles);
   }

   private void createEndpointTables() {

      Statement statement = null;
      try {
         dbConnection.setAutoCommit(false);
         statement = dbConnection.createStatement();
         statement.execute(Queries.CREATE_REQUEST_TBL);
         statement.execute(Queries.CREATE_REQUEST_HEADERS_TBL);
         statement.execute(Queries.CREATE_RESPONSE_TBL);
         statement.execute(Queries.CREATE_RESPONSE_HEADERS_TBL);
         dbConnection.commit();
      } catch (SQLException e) {
         if (dbConnection != null) {
            try {
               dbConnection.rollback();
               System.err.print("Transaction is being rolled back when trying to insert table data: " + e.getMessage());
               System.exit(1);
            } catch (SQLException ex) {
               System.err.print("Could not rollback the transaction: " + ex.getMessage());
               System.exit(1);
            }
         }
      } finally {
         try {
            if (statement != null) {
               statement.close();
            }
            if (dbConnection != null) {
               dbConnection.setAutoCommit(true);
            }
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }
   }

   private void insertTableData(final List<StubHttpLifecycle> httpLifecycles) {

      PreparedStatement requestStatement = null;
      PreparedStatement responseStatement = null;
      PreparedStatement requestHeadStatement = null;
      PreparedStatement responseHeadStatement = null;

      try {
         requestStatement = dbConnection.prepareStatement(Queries.INSERT_INTO_REQUEST_PREP_QRY);
         requestHeadStatement = dbConnection.prepareStatement(Queries.INSERT_INTO_REQUEST_HEAD_PREP_QRY);
         responseStatement = dbConnection.prepareStatement(Queries.INSERT_INTO_RESPONSE_PREP_QRY);
         responseHeadStatement = dbConnection.prepareStatement(Queries.INSERT_INTO_RESPONSE_HEAD_PREP_QRY);

         int requestHeaderCount = 0;
         int responseHeaderCount = 0;

         dbConnection.setAutoCommit(false);
         for (int idx = 0; idx < httpLifecycles.size(); idx++) {
            final StubHttpLifecycle httpLifecycle = httpLifecycles.get(idx);
            insertRequestData(idx, requestStatement, httpLifecycle.getRequest());
            requestHeaderCount = insertHeadersData(requestHeaderCount, idx, requestHeadStatement, httpLifecycle.getRequest().getHeaders());
            insertResponseData(idx, responseStatement, httpLifecycle.getResponse());
            responseHeaderCount = insertHeadersData(responseHeaderCount, idx, responseHeadStatement, httpLifecycle.getResponse().getHeaders());
         }
         dbConnection.commit();

      } catch (SQLException e) {
         if (dbConnection != null) {
            try {
               dbConnection.rollback();
               System.err.print("Transaction is being rolled back when trying to insert table data: " + e.getMessage());
               System.exit(1);
            } catch (SQLException ex) {
               System.err.print("Could not rollback the transaction: " + ex.getMessage());
               System.exit(1);
            }
         }
      } finally {
         try {
            if (requestStatement != null) {
               requestStatement.close();
            }
            if (responseStatement != null) {
               responseStatement.close();
            }
            if (dbConnection != null) {
               dbConnection.setAutoCommit(true);
            }
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }
   }

   private int insertHeadersData(int count, final int parentId, final PreparedStatement headerStatement, final Map<String, String> headers) throws SQLException {
      for (final Map.Entry<String, String> entry : headers.entrySet()) {
         headerStatement.setInt(1, count);
         headerStatement.setInt(2, parentId);
         headerStatement.setString(3, entry.getKey());
         headerStatement.setString(4, entry.getValue());
         headerStatement.executeUpdate();
         count++;
      }

      return count;
   }

   private void insertRequestData(final int index, final PreparedStatement requestStatement, final StubRequest request) throws SQLException {
      requestStatement.setInt(1, index);
      requestStatement.setString(2, request.getUrl());
      requestStatement.setString(3, request.getMethod());
      requestStatement.setString(4, request.getPostBody());
      requestStatement.executeUpdate();
   }

   private void insertResponseData(final int index, final PreparedStatement responseStatement, final StubResponse response) throws SQLException {
      responseStatement.setInt(1, index);
      responseStatement.setString(2, response.getStatus());
      responseStatement.setString(3, response.getBody());
      responseStatement.executeUpdate();
   }

   private void initConnection() {

      if (dbConnection == null) {
         try {
            Class.forName("org.h2.Driver");

            final String url = "jdbc:h2:mem:endpoints;";
            final String init = "INIT=CREATE SCHEMA IF NOT EXISTS ENDPOINTS\\;SET SCHEMA ENDPOINTS;";
            final String options = "DB_CLOSE_DELAY=-1\\;SET DEFAULT_TABLE_TYPE=MEMORY;";
            dbConnection = DriverManager.getConnection(url + init + options, "sa", "");
         } catch (SQLException e) {
            System.err.print("Could not load get DB connection: " + e.getMessage());
            System.exit(1);
         } catch (ClassNotFoundException e) {
            System.err.print("Could not load driver for class org.h2.Driver: " + e.getMessage());
            System.exit(1);
         }
         logger.info("Successfully opened database connection ..");
      }
   }

   public final String getHealthCheck() {
      try {
         final boolean isDbClosed = dbConnection.isClosed();
         final String catalog = dbConnection.getCatalog();
         final String requestQueryResult = executeStatusQuery(
               Queries.SELECT_ALL_FROM_REQUEST,
               TBL_NAME_REQ, TBL_COLUMN_ID, TBL_COLUMN_URL, TBL_COLUMN_METHOD, TBL_COLUMN_POSTBODY);

         final String requestHeadersQueryResult = executeStatusQuery(
               Queries.SELECT_ALL_FROM_REQUEST_HEADERS,
               TBL_NAME_REQ_HEADERS, TBL_COLUMN_ID, TBL_COLUMN_REQUEST_ID, TBL_COLUMN_PARAM, TBL_COLUMN_VALUE);

         final String responseQueryResult = executeStatusQuery(
               Queries.SELECT_ALL_FROM_RESPONSE,
               TBL_NAME_RES, TBL_COLUMN_ID, TBL_COLUMN_STATUS, TBL_COLUMN_BODY);

         final String responseHeadersQueryResult = executeStatusQuery(
               Queries.SELECT_ALL_FROM_RESPONSE_HEADERS,
               TBL_NAME_RES_HEADERS, TBL_COLUMN_ID, TBL_COLUMN_RESPONSE_ID, TBL_COLUMN_PARAM, TBL_COLUMN_VALUE);

         return composeStatusMessage(isDbClosed, catalog,
               requestQueryResult,
               requestHeadersQueryResult,
               responseQueryResult,
               responseHeadersQueryResult);

      } catch (SQLException e) {
         System.err.print("Could not get system status: " + e.getMessage());
      }

      return "Could not get system status, got DB error ..";
   }

   private final String composeStatusMessage(final boolean dbClosed, final String catalog, final String... queryResults) {
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

   public final Map<String, String> findResponseFor(final String method, final String pathInfo) {

      final Map<String, String> responseValues = new HashMap<String, String>();
      responseValues.put("null", "No data found for " + method + " " + pathInfo);

      try {
         final PreparedStatement requestPreparedStatement = dbConnection.prepareStatement(Queries.SELECT_RESPONSE_FOR_REQUEST_PREP_QRY);
         requestPreparedStatement.setString(1, method);
         requestPreparedStatement.setString(2, pathInfo);

         final ResultSet responseSelectResultSet = requestPreparedStatement.executeQuery();
         while (responseSelectResultSet.next()) {
            responseValues.put(TBL_COLUMN_STATUS, responseSelectResultSet.getString(TBL_COLUMN_STATUS));
            responseValues.put(TBL_COLUMN_BODY, responseSelectResultSet.getString(TBL_COLUMN_BODY));
         }
         requestPreparedStatement.close();
      } catch (SQLException e) {
         System.err.print("Could not load response for a given request: " + e.getMessage());
      }
      return responseValues;
   }

   private final String executeStatusQuery(final String rawSQL, final String tableName, String... columnNames) {

      final StringBuilder builder = new StringBuilder();

      try {

         final Statement statement = dbConnection.createStatement();
         final ResultSet resultSet = statement.executeQuery(rawSQL);
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
         statement.close();

         if (!builder.toString().isEmpty()) {
            return builder.toString();
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return String.format("ERR: Could not get system status for query [%s]", rawSQL);
   }
}