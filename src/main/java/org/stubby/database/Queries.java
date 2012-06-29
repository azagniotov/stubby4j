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

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:31 PM
 */
final class Queries {

   static final String CREATE_REQUEST_TBL =
         "CREATE MEMORY TABLE ENDPOINTS.REQUEST " +
               "(ID INTEGER PRIMARY KEY, " +
               "URL VARCHAR(255) DEFAULT NULL, " +
               "METHOD VARCHAR(255) DEFAULT NULL, " +
               "COUNTER INTEGER DEFAULT 0, " +
               "POSTBODY LONGVARCHAR DEFAULT NULL)";

   static final String CREATE_REQUEST_HEADERS_TBL =
         "CREATE MEMORY TABLE ENDPOINTS.REQUEST_HEADERS " +
               "(ID INTEGER PRIMARY KEY, " +
               "REQUEST_ID INTEGER NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL)";

   static final String CREATE_RESPONSE_TBL =
         "CREATE MEMORY TABLE ENDPOINTS.RESPONSE " +
               "(ID INTEGER PRIMARY KEY, " +
               "STATUS VARCHAR(255) DEFAULT NULL, " +
               "BODY LONGVARCHAR DEFAULT NULL)";

   static final String CREATE_RESPONSE_HEADERS_TBL =
         "CREATE MEMORY TABLE ENDPOINTS.RESPONSE_HEADERS " +
               "(ID INTEGER PRIMARY KEY, " +
               "RESPONSE_ID INTEGER NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL)";

   static final String INSERT_INTO_REQUEST_PREP_QRY =
         "INSERT INTO ENDPOINTS.REQUEST (ID, URL, METHOD, POSTBODY) VALUES (?, ?, ?, ?)";

   static final String INSERT_INTO_REQUEST_HEAD_PREP_QRY =
         "INSERT INTO ENDPOINTS.REQUEST_HEADERS (ID, REQUEST_ID, PARAM, VALUE) VALUES (?, ?, ?, ?)";

   static final String INSERT_INTO_RESPONSE_PREP_QRY =
         "INSERT INTO ENDPOINTS.RESPONSE (ID, STATUS, BODY) VALUES (?, ?, ?)";

   static final String INSERT_INTO_RESPONSE_HEAD_PREP_QRY =
         "INSERT INTO ENDPOINTS.RESPONSE_HEADERS (ID, RESPONSE_ID, PARAM, VALUE) VALUES (?, ?, ?, ?)";

   static final String SELECT_ALL_FROM_REQUEST =
         "SELECT * FROM ENDPOINTS.REQUEST";

   static final String SELECT_ALL_FROM_REQUEST_HEADERS =
         "SELECT * FROM ENDPOINTS.REQUEST_HEADERS";

   static final String SELECT_ALL_FROM_RESPONSE =
         "SELECT * FROM ENDPOINTS.RESPONSE";

   static final String SELECT_ALL_FROM_RESPONSE_HEADERS =
         "SELECT * FROM ENDPOINTS.RESPONSE_HEADERS";

   static final String SELECT_RESPONSE_HEADERS_BY_RESPID_PREP_QRY =
         "SELECT * FROM ENDPOINTS.RESPONSE_HEADERS WHERE RESPONSE_ID = ?";

   static final String SELECT_RESPONSE_FOR_GET_REQUEST_PREP_QRY =
         "SELECT RES.ID, RES.STATUS, RES.BODY " +
               "FROM ENDPOINTS.RESPONSE RES " +
               "JOIN ENDPOINTS.REQUEST REQ ON REQ.ID = RES.ID " +
               "WHERE REQ.METHOD = 'GET' AND REQ.URL = ?";

   static final String SELECT_RESPONSE_FOR_POST_REQUEST_PREP_QRY =
         "SELECT RES.ID, RES.STATUS, RES.BODY " +
               "FROM ENDPOINTS.RESPONSE RES " +
               "JOIN ENDPOINTS.REQUEST REQ ON REQ.ID = RES.ID " +
               "WHERE REQ.METHOD = 'POST' AND REQ.URL = ? AND REQ.POSTBODY = ?";

   static final String UPDATE_REQUEST_COUNTER =
         "UPDATE ENDPOINTS.REQUEST SET COUNTER = COUNTER + 1 WHERE ID = ?";

   static final String DROP_SCHEMA = "DROP SCHEMA %s CASCADE";

   static final String SET_SCHEMA = "SET SCHEMA %s";

   static final String CREATE_SCHEMA = "CREATE SCHEMA %s AUTHORIZATION DBA";

   private Queries() {

   }
}
