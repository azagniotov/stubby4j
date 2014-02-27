/*
HTTP stub server written in Java with embedded Jetty

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

/**
 * @author Alexander Zagniotov
 * @since 7/2/12, 12:03 AM
 */
public class NotFoundStubResponse extends StubResponse {

   public NotFoundStubResponse() {
      super("404", null, null, null, null,null,null);
   }

   @Override
   public StubResponseTypes getStubResponseType() {
      return StubResponseTypes.NOTFOUND;
   }
}