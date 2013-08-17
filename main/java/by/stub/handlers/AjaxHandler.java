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
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class AjaxHandler extends AbstractHandler {

   private static final Pattern REGEX_REQUEST_OR_RESPONSE = Pattern.compile("^(request|response)$");
   private static final Pattern REGEX_NUMERIC = Pattern.compile("^[0-9]+$");

   private final StubbedDataManager stubbedDataManager;

   public AjaxHandler(final StubbedDataManager stubbedDataManager) {
      this.stubbedDataManager = stubbedDataManager;
   }

   @Override
   public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
      ConsoleUtils.logIncomingRequest(request);

      baseRequest.setHandled(true);

      final HttpServletResponseWithGetStatus wrapper = new HttpServletResponseWithGetStatus(response);
      wrapper.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
      wrapper.setStatus(HttpStatus.OK_200);

      HandlerUtils.setResponseMainHeaders(wrapper);

      final String[] uriFragments = request.getRequestURI().split("/");
      final int urlFragmentsLength = uriFragments.length;
      final String targetFieldName = uriFragments[urlFragmentsLength - 1];
      final String stubType = uriFragments[urlFragmentsLength - 2];

      if (REGEX_REQUEST_OR_RESPONSE.matcher(stubType).matches()) {

         final int stubHttpCycleIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 3]);
         final StubHttpLifecycle foundStubHttpLifecycle = throwErrorOnNonexistentResourceIndex(wrapper, stubHttpCycleIndex);
         renderAjaxResponseContent(wrapper, stubType, targetFieldName, foundStubHttpLifecycle);

      } else if (REGEX_NUMERIC.matcher(stubType).matches()) {

         final int sequencedResponseId = Integer.parseInt(stubType);
         final int stubHttpCycleIndex = Integer.parseInt(uriFragments[urlFragmentsLength - 4]);
         final StubHttpLifecycle foundStubHttpLifecycle = throwErrorOnNonexistentResourceIndex(wrapper, stubHttpCycleIndex);
         renderAjaxResponseContent(wrapper, targetFieldName, sequencedResponseId, foundStubHttpLifecycle);
      }

      ConsoleUtils.logOutgoingResponse(request.getRequestURI(), wrapper);
   }

   private void renderAjaxResponseContent(final HttpServletResponseWithGetStatus wrapper, final String stubType, final String targetFieldName, final StubHttpLifecycle foundStubHttpLifecycle) throws IOException {
      try {
         final String ajaxResponse = foundStubHttpLifecycle.getAjaxResponseContent(stubType, targetFieldName);
         wrapper.getWriter().println(ajaxResponse);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }
   }

   private void renderAjaxResponseContent(final HttpServletResponseWithGetStatus wrapper, final String targetFieldName, final int sequencedResponseId, final StubHttpLifecycle foundStubHttpLifecycle) throws IOException {
      try {
         final String ajaxResponse = foundStubHttpLifecycle.getAjaxResponseContent(targetFieldName, sequencedResponseId);
         wrapper.getWriter().println(ajaxResponse);
      } catch (final Exception ex) {
         HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
      }
   }

   private StubHttpLifecycle throwErrorOnNonexistentResourceIndex(final HttpServletResponseWithGetStatus wrapper, final int stubHttpCycleIndex) throws IOException {
      final StubHttpLifecycle foundStubHttpLifecycle = stubbedDataManager.getMatchedStubHttpLifecycle(stubHttpCycleIndex);
      if (ObjectUtils.isNull(foundStubHttpLifecycle)) {
         try {
            wrapper.getWriter().println("Resource does not exist for ID: " + stubHttpCycleIndex);
         } catch (final Exception ex) {
            HandlerUtils.configureErrorResponse(wrapper, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.toString());
         }
      }
      return foundStubHttpLifecycle;
   }
}
