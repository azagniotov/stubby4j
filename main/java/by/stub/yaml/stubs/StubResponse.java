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

package by.stub.yaml.stubs;

import by.stub.utils.ObjectUtils;
import by.stub.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubResponse {

   private String status = "200";
   private String body;
   private byte[] file;
   private String latency;
   private Map<String, String> headers = Collections.synchronizedMap(new HashMap<String, String>());

   public StubResponse() {

   }

   public String getStatus() {
      return status;
   }

   public void setStatus(final String status) {
      this.status = status;
   }

   public String getBody() {
      return (StringUtils.isSet(body) ? body : "");
   }


   public void setBody(final String body) {
      this.body = body;
   }

   public Map<String, String> getHeaders() {
      return headers;
   }

   public void setHeaders(final Map<String, String> headers) {
      this.headers = headers;
   }

   public String getLatency() {
      return latency;
   }

   public void setLatency(final String latency) {
      this.latency = latency;
   }

   public void setFile(final byte[] file) {
      this.file = file;
   }

   //Used by reflection when populating stubby admin page with stubbed information
   public byte[] getFile() {
      return file;
   }

   public byte[] getResponseBody() {

      if (ObjectUtils.isNull(file) || file.length == 0) {
         return getBody().getBytes(StringUtils.charsetUTF8());
      }
      return file;
   }

   public boolean hasHeaderLocation() {
      return getHeaders().containsKey("location");
   }

   public StubResponseTypes getStubResponseType() {
      return StubResponseTypes.OK_200;
   }
}