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

package by.stub.handlers;

import by.stub.database.StubbedDataManager;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.ConsoleUtils;
import by.stub.utils.HandlerUtils;
import by.stub.utils.ObjectUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AjaxHandler extends AbstractHandler {

   public static final String NAME = "ajax";

   //Do not remove this constant without changing the example in documentation
   public static final String AJAX_ROOT = "/ajax/resource";
   private final StubbedDataManager stubbedDataManager;

   public AjaxHandler(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request, NAME);

      baseRequest.setHandled(true);
      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);
      wrapper.setContentType(MimeTypes.TEXT_HTML_UTF_8);
      wrapper.setStatus(HttpStatus.OK_200);
      wrapper.setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
      wrapper.setHeader(HttpHeaders.DATE, new Date().toString());
      wrapper.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"); // HTTP 1.1.
      wrapper.setHeader(HttpHeaders.PRAGMA, "no-cache"); // HTTP 1.0.
      wrapper.setDateHeader(HttpHeaders.EXPIRES, 0);

      final String[] uriFragments = request.getRequestURI().split("/");

      final int urlFragmentsLength = uriFragments.length;
      if (uriFragments[urlFragmentsLength - 1] instanceof String) {
         final String stubType = uriFragments[urlFragmentsLength - 1];
         final int stubHttpCycleIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 2]);

         final StubHttpLifecycle foundStubHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(stubHttpCycleIndex);
         if (ObjectUtils.isNull(foundStubHttpLifecycle)) {
            try {
               wrapper.getWriter().println("Resource does not exist for ID: " + stubHttpCycleIndex);
            } catch (final Exception ex) {
               HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
            }
         }

         try {
            final String propertyName = request.getParameter("propertyName");
            final String displayableContent = foundStubHttpLifecycle.getDisplayableContent(stubType, propertyName);
            wrapper.getWriter().println(displayableContent);
         } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
         }
      } else {
         final int sequencedResponseId = Integer.parseInt(uriFragments[urlFragmentsLength - 1]);
         final int stubHttpCycleIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 3]);

         final StubHttpLifecycle foundStubHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(stubHttpCycleIndex);
         if (ObjectUtils.isNull(foundStubHttpLifecycle)) {
            try {
               wrapper.getWriter().println("Resource does not exist for ID: " + stubHttpCycleIndex);
            } catch (final Exception ex) {
               HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
            }
         }

         try {
            final String propertyName = request.getParameter("propertyName");
            final String displayableContent = foundStubHttpLifecycle.getDisplayableContent(propertyName, sequencedResponseId);
            wrapper.getWriter().println(displayableContent);
         } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
         }
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper, AjaxHandler.AJAX_ROOT);
   }
}
