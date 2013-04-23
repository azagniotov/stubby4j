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

package by.stub.handlers.strategy;

import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.yaml.stubs.StubRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface StubResponseHandlingStrategy {
   void handle(final HttpServletResponseWithGetStatus response, final StubRequest assertionStubRequest) throws IOException;
}
