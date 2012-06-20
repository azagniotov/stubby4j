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
import java.util.Collection;
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
   public final static String TBL_COLUMN_HEADERS = "HEADERS";
   private final static String TBL_NAME_REQ_HEADERS = String.format("%s_%s", TBL_NAME_REQ, TBL_COLUMN_HEADERS);
   private final static String TBL_NAME_RES_HEADERS = String.format("%s_%s", TBL_NAME_RES, TBL_COLUMN_HEADERS);
   public final static String TBL_COLUMN_ID = "ID";
   public final static String TBL_COLUMN_STATUS = "STATUS";
   public final static String TBL_COLUMN_BODY = "BODY";
   private final static String TBL_COLUMN_POSTBODY = "POSTBODY";
   public final static String TBL_COLUMN_URL = "URL";
   public final static String TBL_COLUMN_COUNTER = "COUNTER";
   private final static String TBL_COLUMN_METHOD = "METHOD";
   private final static String TBL_COLUMN_RESPONSE_ID = "RESPONSE_ID";
   private final static String TBL_COLUMN_REQUEST_ID = "REQUEST_ID";
   private static final String TBL_COLUMN_PARAM = "PARAM";
   private static final String TBL_COLUMN_VALUE = "VALUE";
   public final static String NOCONTENT_MSG_KEY = "DEFAULT";

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
         runRollback(e);
      } finally {
         runFinally(Arrays.asList(statement));
      }
   }

   private void insertTableData(final List<StubHttpLifecycle> httpLifecycles) {
      final Map<String, Statement> statmnts = new HashMap<String, Statement>();
      try {
         statmnts.put("request", dbConnection.prepareStatement(Queries.INSERT_INTO_REQUEST_PREP_QRY));
         statmnts.put("requestHeaders", dbConnection.prepareStatement(Queries.INSERT_INTO_REQUEST_HEAD_PREP_QRY));
         statmnts.put("response", dbConnection.prepareStatement(Queries.INSERT_INTO_RESPONSE_PREP_QRY));
         statmnts.put("responseHeaders", dbConnection.prepareStatement(Queries.INSERT_INTO_RESPONSE_HEAD_PREP_QRY));

         int requestHeaderCount = 0, responseHeaderCount = 0;

         dbConnection.setAutoCommit(false);
         for (int idx = 0; idx < httpLifecycles.size(); idx++) {
            final StubHttpLifecycle httpLifecycle = httpLifecycles.get(idx);
            final StubRequest request = httpLifecycle.getRequest();
            final StubResponse response = httpLifecycle.getResponse();

            insertRequestData(idx, (PreparedStatement) statmnts.get("request"), request);
            requestHeaderCount = insertHeadersData(requestHeaderCount, idx, (PreparedStatement) statmnts.get("requestHeaders"), request.getHeaders());
            insertResponseData(idx, (PreparedStatement) statmnts.get("response"), response);
            responseHeaderCount = insertHeadersData(responseHeaderCount, idx, (PreparedStatement) statmnts.get("responseHeaders"), response.getHeaders());
         }
         dbConnection.commit();

      } catch (SQLException e) {
         runRollback(e);
      } finally {
         runFinally(statmnts.values());
      }
   }

   private void runRollback(final SQLException e) {
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
   }

   private void runFinally(final Collection<Statement> statements) {
      try {
         for (final Statement statement : statements) {
            if (statement != null) {
               statement.close();
            }
         }
         if (dbConnection != null) {
            dbConnection.setAutoCommit(true);
         }
      } catch (SQLException e) {
         e.printStackTrace();
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

   public final List<List<Map<String, Object>>> getHttpConfigData() {
      final List<List<Map<String, Object>>> data = new LinkedList<List<Map<String, Object>>>();
      try {
         final Statement statement = dbConnection.createStatement();

         final String[] queries = {Queries.SELECT_ALL_FROM_REQUEST,
               Queries.SELECT_ALL_FROM_REQUEST_HEADERS,
               Queries.SELECT_ALL_FROM_RESPONSE,
               Queries.SELECT_ALL_FROM_RESPONSE_HEADERS};

         for (final String query : queries) {
            final ResultSet resultSet = statement.executeQuery(query);
            data.add(ResultSetConverter.convertResultSetToMap(resultSet));
         }
         statement.close();

         return data;

      } catch (SQLException e) {
         System.err.print("Could not get system status: " + e.getMessage());
      }

      return data;
   }

   public final Map<String, String> retrieveResponseFor(final String requestPathinfo, final String method, final String postBody) {

      Map<String, String> responseValues = new HashMap<String, String>();
      final String postMessage = (postBody != null ? " for post data: " + postBody : "");
      responseValues.put(NOCONTENT_MSG_KEY, "No data found for " + method + " request at URI " + requestPathinfo + postMessage);

      try {

         final String query = identifyQueryByHttpMethod(method);
         final PreparedStatement responseStatement = buildPreparedstatement(requestPathinfo, method, postBody, query);
         final ResultSet responseResultSet = responseStatement.executeQuery();

         while (responseResultSet.next()) {
            responseValues.put(TBL_COLUMN_STATUS, responseResultSet.getString(TBL_COLUMN_STATUS));
            responseValues.put(TBL_COLUMN_BODY, responseResultSet.getString(TBL_COLUMN_BODY));

            final int responseID = responseResultSet.getInt(TBL_COLUMN_ID);
            responseValues = getResponseHeaders(responseValues, responseID);

            updateRequestCounter(responseID);
         }
         responseStatement.close();
      } catch (SQLException e) {
         e.printStackTrace();
         System.err.print("Could not load response for a given request: " + e.getMessage());
      }
      return responseValues;
   }

   private void updateRequestCounter(final int responseID) throws SQLException {
      final PreparedStatement responseStatement = dbConnection.prepareStatement(Queries.UPDATE_REQUEST_COUNTER);
      responseStatement.setInt(1, responseID);
      responseStatement.executeUpdate();
   }

   private String identifyQueryByHttpMethod(final String method) {
      return (method.toLowerCase().equals("get") ?
            Queries.SELECT_RESPONSE_FOR_GET_REQUEST_PREP_QRY :
            Queries.SELECT_RESPONSE_FOR_POST_REQUEST_PREP_QRY);
   }

   private PreparedStatement buildPreparedstatement(final String requestPathinfo, final String method, final String postBody, final String query) throws SQLException {
      final PreparedStatement responseStatement = dbConnection.prepareStatement(query);
      responseStatement.setString(1, requestPathinfo);
      if (method.toLowerCase().equals("post")) {
         responseStatement.setString(2, postBody);
      }
      return responseStatement;
   }

   private Map<String, String> getResponseHeaders(final Map<String, String> responseValues, final int responseID) throws SQLException {
      final PreparedStatement responseHeadersStatement = dbConnection.prepareStatement(Queries.SELECT_RESPONSE_HEADERS_BY_RESPID_PREP_QRY);
      responseHeadersStatement.setInt(1, responseID);
      final ResultSet responseHeadersResultSet = responseHeadersStatement.executeQuery();

      while (responseHeadersResultSet.next()) {
         responseValues.put(
               responseHeadersResultSet.getString(TBL_COLUMN_PARAM),
               responseHeadersResultSet.getString(TBL_COLUMN_VALUE));
      }

      return responseValues;
   }
}