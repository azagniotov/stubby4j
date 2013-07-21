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

package by.stub.handlers.strategy.stubs;

import by.stub.exception.Stubby4JException;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class DefaultResponseHandlingStrategy implements StubResponseHandlingStrategy {

   private final StubResponse foundStubResponse;

   public DefaultResponseHandlingStrategy(final StubResponse foundStubResponse) {
      this.foundStubResponse = foundStubResponse;
   }

   @Override
   public void handle(final HttpServletResponseWithGetStatus response, final StubRequest assertionStubRequest) throws IOException {
      HandlerUtils.setResponseMainHeaders(response);
      setStubResponseHeaders(foundStubResponse, response);

      if (StringUtils.isSet(foundStubResponse.getLatency())) {
         try {
            final long latency = Long.parseLong(foundStubResponse.getLatency());
            TimeUnit.MILLISECONDS.sleep(latency);
         } catch (final InterruptedException e) {
            throw new Stubby4JException(e);
         }
      }
      response.setStatus(Integer.parseInt(foundStubResponse.getStatus()));

      final byte[] responseBody = foundStubResponse.getResponseBody();

      String evaluatedResponseBody = templatizeResponse(new String(responseBody), assertionStubRequest);

      final OutputStream streamOut = response.getOutputStream();
      streamOut.write(evaluatedResponseBody.getBytes());
      streamOut.flush();
      streamOut.close();
   }

   private String templatizeResponse(final String response, StubRequest request) {
      Configuration cfg = new Configuration();
      cfg.setObjectWrapper(new DefaultObjectWrapper());
      cfg.setDefaultEncoding("UTF-8");

      Map root = new HashMap();
      root.put("requestBody", request.getPostBody());
      root.put("requestParams", request.getQuery());
      try {
         Template temp = new Template("stub", new StringReader(response), new Configuration());
         Writer out = new StringWriter();
         temp.process(root, out);
         return out.toString();
      } catch (Exception
         e) {
         throw new RuntimeException(e);
      }
   }

   private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
      response.setCharacterEncoding(StringUtils.UTF_8);
      for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
         response.setHeader(entry.getKey(), entry.getValue());
      }
   }
}
