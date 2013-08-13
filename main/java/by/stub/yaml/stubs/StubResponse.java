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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Zagniotov
 * @since 6/14/12, 1:09 AM
 */
public class StubResponse {

   private final String status;
   private final String body;
   private final byte[] file;
   private final String latency;
   private final Map<String, String> headers;

   public StubResponse(final String status,
                       final String body,
                       final byte[] file,
                       final String latency,
                       final Map<String, String> headers) {
      this.status = ObjectUtils.isNull(status) ? "200" : status;
      this.body = body;
      this.file = file;
      this.latency = latency;
      this.headers =  ObjectUtils.isNull(headers) ? new HashMap<String, String>() : headers;
   }

   public String getStatus() {
      return status;
   }

   public String getBody() {
      return (StringUtils.isSet(body) ? body : "");
   }

   public Map<String, String> getHeaders() {
      return headers;
   }

   public String getLatency() {
      return latency;
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

   public static StubResponse newStubResponse() {
      return new StubResponse(null, null, null, null, null);
   }

   public static StubResponse newStubResponse(final String status, final String body) {
      return new StubResponse(status, body, null, null, null);
   }
}