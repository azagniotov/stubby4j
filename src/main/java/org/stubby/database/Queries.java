package org.stubby.database;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:31 PM
 */
final class Queries {
   public static final String CREATE_REQUEST_TBL =
         "CREATE TABLE ENDPOINTS.REQUEST " +
               "(ID INT PRIMARY KEY, " +
               "URL VARCHAR(255) DEFAULT NULL, " +
               "METHOD VARCHAR(255) DEFAULT NULL, " +
               "POSTBODY TEXT DEFAULT NULL) NOT PERSISTENT";

   public static final String CREATE_REQUEST_HEADERS_TBL =
         "CREATE TABLE ENDPOINTS.REQUEST_HEADERS " +
               "(ID INT PRIMARY KEY, " +
               "REQUEST_ID INT(32) NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT";

   public static final String CREATE_RESPONSE_TBL =
         "CREATE TABLE ENDPOINTS.RESPONSE " +
               "(ID INT PRIMARY KEY, " +
               "STATUS VARCHAR(255) DEFAULT NULL, " +
               "BODY TEXT DEFAULT NULL) NOT PERSISTENT";

   public static final String CREATE_RESPONSE_HEADERS_TBL =
         "CREATE TABLE ENDPOINTS.RESPONSE_HEADERS " +
               "(ID INT PRIMARY KEY, " +
               "RESPONSE_ID INT(32) NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT";

   public static final String INSERT_INTO_REQUEST_PREP_QRY =
         "INSERT INTO ENDPOINTS.REQUEST (ID, URL, METHOD, POSTBODY) VALUES (?, ?, ?, ?)";

   public static final String INSERT_INTO_REQUEST_HEAD_PREP_QRY =
         "INSERT INTO ENDPOINTS.REQUEST_HEADERS (ID, REQUEST_ID, PARAM, VALUE) VALUES (?, ?, ?, ?)";

   public static final String INSERT_INTO_RESPONSE_PREP_QRY =
         "INSERT INTO ENDPOINTS.RESPONSE (ID, STATUS, BODY) VALUES (?, ?, ?)";

   public static final String INSERT_INTO_RESPONSE_HEAD_PREP_QRY =
         "INSERT INTO ENDPOINTS.RESPONSE_HEADERS (ID, RESPONSE_ID, PARAM, VALUE) VALUES (?, ?, ?, ?)";

   public static final String SELECT_ALL_FROM_REQUEST =
         "SELECT * FROM ENDPOINTS.REQUEST";

   public static final String SELECT_ALL_FROM_REQUEST_HEADERS =
         "SELECT * FROM ENDPOINTS.REQUEST_HEADERS";

   public static final String SELECT_ALL_FROM_RESPONSE =
         "SELECT * FROM ENDPOINTS.RESPONSE";

   public static final String SELECT_ALL_FROM_RESPONSE_HEADERS =
         "SELECT * FROM ENDPOINTS.RESPONSE_HEADERS";

   public static final String SELECT_RESPONSE_FOR_REQUEST_PREP_QRY =
         "SELECT RES.STATUS, RES.BODY " +
               "FROM ENDPOINTS.RESPONSE RES " +
               "JOIN ENDPOINTS.REQUEST REQ ON REQ.ID = RES.ID " +
               "WHERE REQ.METHOD = ? AND REQ.URL = ?";

   private Queries() {

   }
}
