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
         "CREATE TABLE ENDPOINTS.REQUEST " +
               "(ID INT PRIMARY KEY, " +
               "URL VARCHAR(255) DEFAULT NULL, " +
               "METHOD VARCHAR(255) DEFAULT NULL, " +
               "POSTBODY TEXT DEFAULT NULL) NOT PERSISTENT";

   static final String CREATE_REQUEST_HEADERS_TBL =
         "CREATE TABLE ENDPOINTS.REQUEST_HEADERS " +
               "(ID INT PRIMARY KEY, " +
               "REQUEST_ID INT(32) NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT";

   static final String CREATE_RESPONSE_TBL =
         "CREATE TABLE ENDPOINTS.RESPONSE " +
               "(ID INT PRIMARY KEY, " +
               "STATUS VARCHAR(255) DEFAULT NULL, " +
               "BODY TEXT DEFAULT NULL) NOT PERSISTENT";

   static final String CREATE_RESPONSE_HEADERS_TBL =
         "CREATE TABLE ENDPOINTS.RESPONSE_HEADERS " +
               "(ID INT PRIMARY KEY, " +
               "RESPONSE_ID INT(32) NOT NULL, " +
               "PARAM VARCHAR(255) DEFAULT NULL, " +
               "VALUE VARCHAR(255) DEFAULT NULL) NOT PERSISTENT";

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

   static final String SELECT_RESPONSE_FOR_GET_REQUEST_PREP_QRY =
         "SELECT RES.STATUS, RES.BODY " +
               "FROM ENDPOINTS.RESPONSE RES " +
               "JOIN ENDPOINTS.REQUEST REQ ON REQ.ID = RES.ID " +
               "WHERE REQ.METHOD = 'GET' AND REQ.URL = ?";

   static final String SELECT_RESPONSE_FOR_POST_REQUEST_PREP_QRY =
         "SELECT RES.STATUS, RES.BODY " +
               "FROM ENDPOINTS.RESPONSE RES " +
               "JOIN ENDPOINTS.REQUEST REQ ON REQ.ID = RES.ID " +
               "WHERE REQ.METHOD = 'POST' AND REQ.URL = ? AND REQ.POSTBODY = ?";

   private Queries() {

   }
}
